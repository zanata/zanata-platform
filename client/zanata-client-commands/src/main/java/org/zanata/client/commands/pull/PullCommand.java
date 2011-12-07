package org.zanata.client.commands.pull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PullCommand extends PushPullCommand<PullOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PullCommand.class);
   private static final String UTF_8 = "UTF-8";

   private static final Map<String, PullStrategy> strategies = new HashMap<String, PullStrategy>();

   {
      strategies.put(PROJECT_TYPE_UTF8_PROPERTIES, new PropertiesStrategy(UTF_8));
      strategies.put(PROJECT_TYPE_PROPERTIES, new PropertiesStrategy());
      strategies.put(PROJECT_TYPE_PUBLICAN, new GettextDirStrategy());
      strategies.put(PROJECT_TYPE_XLIFF, new XliffStrategy());
      strategies.put(PROJECT_TYPE_XML, new XmlStrategy());
   }

   public PullCommand(PullOptions opts)
   {
      super(opts);
   }

   public PullCommand(PullOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory, translationResources, uri);
   }

   private PullStrategy getStrategy(String strategyType)
   {
      PullStrategy strat = strategies.get(strategyType);
      if (strat == null)
      {
         throw new RuntimeException("unknown project type: " + getOpts().getProjectType());
      }
      strat.setPullOptions(getOpts());
      return strat;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      if (getOpts().getPullSrc())
      {
         log.info("Pulling source and target (translation) documents");
         log.info("Source-language directory (originals): {}", getOpts().getSrcDir());
      }
      else
      {
         log.info("Pulling target documents (translations) only");
      }
      log.info("Target-language base directory (translations): {}", getOpts().getTransDir());

      if (getOpts().isDryRun())
      {
         log.info("DRY RUN: no permanent changes will be made");
      }
      if (getOpts().getPullSrc())
      {
         log.warn("pullSrc option is set: existing source-language files may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
      }
      PullStrategy strat = getStrategy(getOpts().getProjectType());

      LocaleList locales = getOpts().getLocales();
      if (locales == null)
         throw new ConfigException("no locales specified");

      ClientResponse<List<ResourceMeta>> listResponse = translationResources.get(null);
      ClientUtility.checkResult(listResponse, uri);
      List<ResourceMeta> resourceMetaList = listResponse.getEntity();
      for (ResourceMeta resourceMeta : resourceMetaList)
      {
         Resource doc = null;
         String docName = resourceMeta.getName();
         // TODO follow a Link
         String docUri = RestUtil.convertToDocumentURIId(docName);
         if (strat.needsDocToWriteTrans() || getOpts().getPullSrc())
         {
            ClientResponse<Resource> resourceResponse = translationResources.getResource(docUri, strat.getExtensions());
            ClientUtility.checkResult(resourceResponse, uri);
            doc = resourceResponse.getEntity();
         }
         if (getOpts().getPullSrc())
         {
            if (!getOpts().isDryRun())
            {
               log.info("writing source file for document {}", docName);
               strat.writeSrcFile(doc);
            }
            else
            {
               log.info("writing source file for document {} (skipped due to dry run)", docName);
            }
         }

         for (LocaleMapping locMapping : locales)
         {
            LocaleId locale = new LocaleId(locMapping.getLocale());

            ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(docUri, locale, strat.getExtensions());
            // ignore 404 (no translation yet for specified document)
            if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND)
            {
               log.info("no translations found in locale {} for document {}", locale, docName);
               continue;
            }
            ClientUtility.checkResult(transResponse, uri);
            TranslationsResource targetDoc = transResponse.getEntity();

            if (!getOpts().isDryRun())
            {
               log.info("writing translation file in locale {} for document {}", locMapping.getLocalLocale(), docName);
               strat.writeTransFile(doc, docName, locMapping, targetDoc);
            }
            else
            {
               log.info("writing translation file in locale {} for document {} (skipped due to dry run)", locMapping.getLocalLocale(), docName);
            }
         }
      }

   }

}
