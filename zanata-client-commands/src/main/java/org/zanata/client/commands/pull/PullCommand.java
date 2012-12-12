package org.zanata.client.commands.pull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import org.zanata.client.commands.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
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

   private static final Map<String, Class<? extends PullStrategy>> strategies = new HashMap<String, Class<? extends PullStrategy>>();

   {
      strategies.put(PROJECT_TYPE_UTF8_PROPERTIES, UTF8PropertiesStrategy.class);
      strategies.put(PROJECT_TYPE_PROPERTIES, PropertiesStrategy.class);
      strategies.put(PROJECT_TYPE_GETTEXT, GettextPullStrategy.class);
      strategies.put(PROJECT_TYPE_PUBLICAN, GettextDirStrategy.class);
      strategies.put(PROJECT_TYPE_XLIFF, XliffStrategy.class);
      strategies.put(PROJECT_TYPE_XML, XmlStrategy.class);
   }

   public PullCommand(PullOptions opts)
   {
      super(opts);
   }

   public PullCommand(PullOptions opts, ZanataProxyFactory factory, ISourceDocResource sourceDocResource, ITranslatedDocResource translationResources, URI uri)
   {
      super(opts, factory, sourceDocResource, translationResources, uri);
   }

   private PullStrategy createStrategy(String strategyType)
           throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
   {
      Class<? extends PullStrategy> clazz = strategies.get(strategyType);
      if (clazz == null)
      {
         throw new RuntimeException("unknown project type: " + getOpts().getProjectType());
      }
      Constructor<? extends PullStrategy> ctor = clazz.getConstructor(PullOptions.class);
      assert ctor != null: "strategy must have constructor which accepts PullOptions";
      return ctor.newInstance(getOpts());
   }

   private void logOptions()
   {
      logOptions(log, getOpts());
      log.info("Create skeletons for untranslated messages/files: {}", getOpts().getCreateSkeletons());

      if (getOpts().isDryRun())
      {
         log.info("DRY RUN: no permanent changes will be made");
      }
   }

   /**
    * @param logger
    * @param opts
    */
   public static void logOptions(Logger logger, PullOptions opts)
   {
      logger.info("Server: {}", opts.getUrl());
      logger.info("Project: {}", opts.getProj());
      logger.info("Version: {}", opts.getProjectVersion());
      logger.info("Username: {}", opts.getUsername());
      logger.info("Project type: {}", opts.getProjectType());
      logger.info("Enable modules: {}", opts.getEnableModules());
      if (opts.getEnableModules())
      {
         logger.info("Current Module: {}", opts.getCurrentModule());
         if (opts.isRootModule())
         {
            logger.info("Root module: YES");
            if (logger.isDebugEnabled())
            {
               logger.debug("Modules: {}", StringUtils.join(opts.getAllModules(), ", "));
            }
         }
      }
      logger.info("Locales to pull: {}", opts.getLocaleMapList());
      logger.info("Encode tab as \\t: {}", opts.getEncodeTabs());
      logger.info("Current directory: {}", System.getProperty("user.dir"));
      if (opts.getPullType() == PushPullType.Source)
      {
         logger.info("Pulling source documents only");
         logger.info("Source-language directory (originals): {}", opts.getSrcDir());
      }
      else if( opts.getPullType() == PushPullType.Trans )
      {
         logger.info("Pulling target documents (translations) only");
         logger.info("Target-language base directory (translations): {}", opts.getTransDir());
      }
      else
      {
         logger.info("Pulling source and target (translation) documents");
         logger.info("Source-language directory (originals): {}", opts.getSrcDir());
         logger.info("Target-language base directory (translations): {}", opts.getTransDir());
      }
   }

   @Override
   public void run() throws Exception
   {
      logOptions();

      LocaleList locales = getOpts().getLocaleMapList();
      if (locales == null)
         throw new ConfigException("no locales specified");
      PullStrategy strat = createStrategy(getOpts().getProjectType());
      List<String> docNamesForModule = getQualifiedDocNamesForCurrentModuleFromServer();

      // TODO compare docNamesForModule with localDocNames, offer to delete obsolete translations from filesystem
      if (docNamesForModule.isEmpty())
      {
         log.info("No documents in remote module: {}; nothing to do", getOpts().getCurrentModule());
         return;
      }
      log.info("Pulling {} docs for this module from the server", docNamesForModule.size());
      log.debug("Doc names: {}", docNamesForModule);

      PushPullType pullType = getOpts().getPullType();
      boolean pullSrc = pullType == PushPullType.Both || pullType == PushPullType.Source;
      boolean pullTarget = pullType == PushPullType.Both || pullType == PushPullType.Trans;

      if (pullSrc)
      {
         log.warn("Pull Type set to '" + pullType + "': existing source-language files may be overwritten/deleted");
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
         boolean createSkeletons = getOpts().getCreateSkeletons();
         if (strat.needsDocToWriteTrans() || pullSrc || createSkeletons)
         {
            ClientResponse<Resource> resourceResponse = sourceDocResource.getResource(docUri, strat.getExtensions());
            ClientUtility.checkResult(resourceResponse, uri);
            doc = resourceResponse.getEntity();
            doc.setName(localDocName);
         }
         if (pullSrc)
         {
            writeSrcDoc(strat, doc);
         }

         if( pullTarget )
         {
            for (LocaleMapping locMapping : locales)
            {
               LocaleId locale = new LocaleId(locMapping.getLocale());

               ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(
                     docUri, locale, strat.getExtensions(), createSkeletons);
               TranslationsResource targetDoc;
               // ignore 404 (no translation yet for specified document)
               if (transResponse.getResponseStatus() == Response.Status.NOT_FOUND)
               {
                  targetDoc = null;
                  if (!createSkeletons)
                  {
                     log.info("No translations found in locale {} for document {}", locale, localDocName);
                     continue;
                  }
               }
               else
               {
                  ClientUtility.checkResult(transResponse, uri);
                  targetDoc = transResponse.getEntity();
               }
               if (targetDoc != null || createSkeletons)
               {
                  writeTargetDoc(strat, localDocName, locMapping, doc, targetDoc);
               }
            }
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

   /**
    * 
    * @param strat
    * @param localDocName
    * @param locMapping
    * @param docWithLocalName may be null if needsDocToWriteTrans() returns false
    * @param targetDoc
    * @throws IOException
    */
   private void writeTargetDoc(
         PullStrategy strat,
         String localDocName,
         LocaleMapping locMapping,
         Resource docWithLocalName,
         TranslationsResource targetDoc) throws IOException
   {
      if (!getOpts().isDryRun())
      {
         log.info("Writing translation file in locale {} for document {}", locMapping.getLocalLocale(), localDocName);
         strat.writeTransFile(docWithLocalName, localDocName, locMapping, targetDoc);
      }
      else
      {
         log.info("Writing translation file in locale {} for document {} (skipped due to dry run)", locMapping.getLocalLocale(), localDocName);
      }
   }

}
