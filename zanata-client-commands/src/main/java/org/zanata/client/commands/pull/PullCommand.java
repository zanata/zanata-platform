package org.zanata.client.commands.pull;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
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

   private void logOptions()
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      log.info("Project type: {}", getOpts().getProjectType());
      log.info("Enable modules: {}", getOpts().getEnableModules());
      if (getOpts().getEnableModules())
      {
         log.info("Current Module: {}", getOpts().getCurrentModule());
         if (getOpts().isRootModule())
         {
            log.info("Root module: YES");
            if (log.isDebugEnabled())
            {
               log.debug("Modules: {}", StringUtils.join(getOpts().getAllModules(), ", "));
            }
         }
      }
      log.info("Locales to pull: {}", getOpts().getLocales());
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
   }

   @Override
   public void run() throws Exception
   {
      logOptions();

      LocaleList locales = getOpts().getLocales();
      if (locales == null)
         throw new ConfigException("no locales specified");
      PullStrategy strat = getStrategy(getOpts().getProjectType());
      List<String> docNamesForModule = getQualifiedDocNamesForCurrentModuleFromServer();

      // TODO compare docNamesForModule with localDocNames, offer to delete obsolete translations from filesystem
      if (docNamesForModule.isEmpty())
      {
         log.info("No documents in remote module: {}; nothing to do", getOpts().getCurrentModule());
         return;
      }
      log.info("Pulling {} docs for this module from the server", docNamesForModule.size());
      log.debug("Doc names: {}", docNamesForModule);

      if (getOpts().getPullSrc())
      {
         log.warn("The pullSrc option is set: existing source-language files may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents and translations in the above directories.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing translations in the above directory.\n");
      }

      for (String qualifiedDocName : docNamesForModule)
      {
         Resource doc = null;
         String localDocName = unqualifiedDocName(qualifiedDocName);
         // TODO follow a Link instead of generating the URI
         String docUri = RestUtil.convertToDocumentURIId(qualifiedDocName);
         if (strat.needsDocToWriteTrans() || getOpts().getPullSrc())
         {
            ClientResponse<Resource> resourceResponse = translationResources.getResource(docUri, strat.getExtensions());
            ClientUtility.checkResult(resourceResponse, uri);
            doc = resourceResponse.getEntity();
            doc.setName(localDocName);
         }
         if (getOpts().getPullSrc())
         {
            writeSrcDoc(strat, doc);
         }

         for (LocaleMapping locMapping : locales)
         {
            LocaleId locale = new LocaleId(locMapping.getLocale());

            ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(docUri, locale, strat.getExtensions());
            // ignore 404 (no translation yet for specified document)
            if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND)
            {
               log.info("No translations found in locale {} for document {}", locale, localDocName);
               continue;
            }
            ClientUtility.checkResult(transResponse, uri);
            TranslationsResource targetDoc = transResponse.getEntity();

            writeTargetDoc(strat, localDocName, locMapping, doc, targetDoc);
         }
      }

   }

   private void writeSrcDoc(PullStrategy strat, Resource doc) throws IOException
   {
      if (!getOpts().isDryRun())
      {
         log.info("Writing source file for document {}", doc.getName());
         strat.writeSrcFile(doc);
      }
      else
      {
         log.info("Writing source file for document {} (skipped due to dry run)", doc.getName());
      }
   }

   private void writeTargetDoc(PullStrategy strat, String localDocName, LocaleMapping locMapping, Resource doc, TranslationsResource targetDoc) throws IOException
   {
      if (!getOpts().isDryRun())
      {
         log.info("Writing translation file in locale {} for document {}", locMapping.getLocalLocale(), localDocName);
         strat.writeTransFile(doc, localDocName, locMapping, targetDoc);
      }
      else
      {
         log.info("Writing translation file in locale {} for document {} (skipped due to dry run)", locMapping.getLocalLocale(), localDocName);
      }
   }

}
