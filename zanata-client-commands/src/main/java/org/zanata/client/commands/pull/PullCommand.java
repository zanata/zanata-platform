package org.zanata.client.commands.pull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.push.PushPullType;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.etag.ETagCacheEntry;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.common.io.FileDetails;
import org.zanata.rest.RestUtil;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.HashUtil;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PullCommand extends PushPullCommand<PullOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PullCommand.class);

   private static final Map<String, PullStrategy> strategies = new HashMap<String, PullStrategy>();

   {
      strategies.put(PROJECT_TYPE_UTF8_PROPERTIES, new UTF8PropertiesStrategy());
      strategies.put(PROJECT_TYPE_PROPERTIES, new PropertiesStrategy());
      strategies.put(PROJECT_TYPE_GETTEXT, new GettextPullStrategy());
      strategies.put(PROJECT_TYPE_PUBLICAN, new GettextDirStrategy());
      strategies.put(PROJECT_TYPE_XLIFF, new XliffStrategy());
      strategies.put(PROJECT_TYPE_XML, new XmlStrategy());
   }

   public PullCommand(PullOptions opts)
   {
      super(opts);
   }

   public PullCommand(PullOptions opts, ZanataProxyFactory factory, ISourceDocResource sourceDocResource, ITranslatedDocResource translationResources, URI uri)
   {
      super(opts, factory, sourceDocResource, translationResources, uri);
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
      logger.info("Using ETag cache: {}", opts.getUseCache());
      logger.info("Purging ETag cache beforehand: {}", opts.getPurgeCache());
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

      if( getOpts().getPurgeCache() )
      {
         eTagCache.clear();
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
               String eTag = null;
               File transFile = strat.getTransFileToWrite(localDocName, locMapping);
               ETagCacheEntry eTagCacheEntry = eTagCache.findEntry(localDocName, locale.getId());

               if( getOpts().getUseCache() && eTagCacheEntry != null )
               {
                  // Check the last updated date on the file matches what's in the cache
                  // only then use the cached ETag
                  if( transFile.exists() && Long.toString(transFile.lastModified()).equals( eTagCacheEntry.getLocalFileTime() ) )
                  {
                     eTag = eTagCacheEntry.getServerETag();
                  }
               }

               ClientResponse<TranslationsResource> transResponse = translationResources.getTranslations(
                     docUri, locale, strat.getExtensions(), createSkeletons, eTag);
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
               // 304 NOT MODIFIED (the document can stay the same)
               else if(transResponse.getResponseStatus() == Response.Status.NOT_MODIFIED)
               {
                  targetDoc = null;
                  log.info("No changes in translations for locale {} and document {}", locale, localDocName);

                  // Check the file's MD5 matches what's stored in the cache. If not, it needs to be fetched again (with no etag)
                  String fileChecksum = HashUtil.getMD5Checksum( transFile );
                  if( !fileChecksum.equals( eTagCacheEntry.getLocalFileMD5() ) )
                  {
                     transResponse = translationResources.getTranslations(
                           docUri, locale, strat.getExtensions(), createSkeletons, null);
                     ClientUtility.checkResult(transResponse, uri);
                     targetDoc = transResponse.getEntity();
                  }
               }
               else
               {
                  ClientUtility.checkResult(transResponse, uri);
                  targetDoc = transResponse.getEntity();
               }
               if (targetDoc != null || createSkeletons)
               {
                  writeTargetDoc(strat, localDocName, locMapping, doc, targetDoc, transResponse.getHeaders().getFirst(HttpHeaders.ETAG));
               }
            }

            // write the cache
            super.storeETagCache();
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
         TranslationsResource targetDoc,
         String serverETag) throws IOException
   {
      if (!getOpts().isDryRun())
      {
         log.info("Writing translation file in locale {} for document {}", locMapping.getLocalLocale(), localDocName);
         FileDetails fileDetails = strat.writeTransFile(docWithLocalName, localDocName, locMapping, targetDoc);

         // Insert to cache if the strategy returned file details and we are using the cache
         if( getOpts().getUseCache() && fileDetails != null )
         {
            eTagCache.addEntry( new ETagCacheEntry(
                  localDocName,
                  locMapping.getLocale(),
                  Long.toString(fileDetails.getFile().lastModified()),
                  fileDetails.getMd5(),
                  serverETag) );
         }
      }
      else
      {
         log.info("Writing translation file in locale {} for document {} (skipped due to dry run)", locMapping.getLocalLocale(), localDocName);
      }
   }

}
