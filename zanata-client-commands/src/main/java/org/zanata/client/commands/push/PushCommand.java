package org.zanata.client.commands.push;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
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
public class PushCommand extends PushPullCommand<PushOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PushCommand.class);

   private static final Map<String, AbstractPushStrategy> strategies = new HashMap<String, AbstractPushStrategy>();

   public static interface TranslationResourcesVisitor
   {
      void visit(LocaleMapping locale, TranslationsResource targetDoc);
   }

   {
      strategies.put(PROJECT_TYPE_PROPERTIES, new PropertiesStrategy());
      strategies.put(PROJECT_TYPE_PUBLICAN, new GettextDirStrategy());
      strategies.put(PROJECT_TYPE_XLIFF, new XliffStrategy());
      strategies.put(PROJECT_TYPE_XML, new XmlStrategy());
   }

   public PushCommand(PushOptions opts)
   {
      super(opts);
   }

   public PushCommand(PushOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory, translationResources, uri);
   }

   private AbstractPushStrategy getStrategy(String strategyType)
   {
      AbstractPushStrategy strat = strategies.get(strategyType);
      if (strat == null)
      {
         throw new RuntimeException("unknown project type: " + getOpts().getProjectType());
      }
      strat.setPushOptions(getOpts());
      return strat;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      log.info("Project type: {}", getOpts().getProjectType());
      log.info("Source language: {}", getOpts().getSourceLang());
      log.info("Copy previous translations: {}", getOpts().getCopyTrans());
      log.info("Merge type: {}", getOpts().getMergeType());

      if (!getOpts().getIncludes().isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         for (String pattern : getOpts().getIncludes())
         {
            sb.append(pattern);
            sb.append(" ");
         }
         log.info("Include patterns: {}", sb.toString());
      }

      if (!getOpts().getExcludes().isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         for(String pattern:getOpts().getExcludes())
         {
            sb.append(pattern);
            sb.append(" ");
         }
         log.info("Exclude patterns: {}", sb.toString());
      }
      log.info("Default exclude: {}", getOpts().getDefaultExcludes());

      if (getOpts().getPushTrans())
      {
         log.info("Pushing source and target documents");
      }
      else
      {
         log.info("Pushing source documents only");
      }
      log.info("Source directory (originals): {}", getOpts().getSrcDir());
      if (getOpts().getPushTrans())
      {
         log.info("Target base directory (translations): {}", getOpts().getTransDir());
      }
      File sourceDir = getOpts().getSrcDir();

      if (!sourceDir.exists())
      {
         throw new RuntimeException("directory '" + sourceDir + "' does not exist - check sourceDir option");
      }

      if (getOpts().getPushTrans())
      {
         if (getOpts().getLocales() == null)
            throw new ConfigException("pushTrans option set, but zanata.xml contains no <locales>");
         log.warn("pushTrans option is set: existing translations on server may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents AND TRANSLATIONS on the server.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing documents on the server.\n");
      }

      AbstractPushStrategy strat = getStrategy(getOpts().getProjectType());

      // NB we don't load all the docs into a HashMap, because that would waste
      // memory

      Set<String> localDocNames = strat.findDocNames(sourceDir, getOpts().getIncludes(), getOpts().getExcludes(), getOpts().getDefaultExcludes());
      for (String docName : localDocNames)
      {
         log.info("Source file to be uploaded: {}", docName);
      }
      deleteObsoleteDocsFromServer(localDocNames);

      for (String docName : localDocNames)
      {
         final String docUri = RestUtil.convertToDocumentURIId(docName);
         final Resource srcDoc = strat.loadSrcDoc(sourceDir, docName);
         debug(srcDoc);

         final StringSet extensions = strat.getExtensions();
         log.info("pushing source document [name={}] to server", srcDoc.getName());
         boolean copyTrans = getOpts().getCopyTrans();
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions, copyTrans);
         ClientUtility.checkResult(putResponse, uri);

         if (getOpts().getPushTrans())
         {
            strat.visitTranslationResources(docName, srcDoc, new TranslationResourcesVisitor()
            {
               @Override
               public void visit(LocaleMapping locale, TranslationsResource targetDoc)
               {
                  debug(targetDoc);
                  log.info("pushing target document [name={} client-locale={}] to server [locale={}]", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
                  ClientResponse<String> putTransResponse = translationResources.putTranslations(docUri, new LocaleId(locale.getLocale()), targetDoc, extensions, getOpts().getMergeType());
                  ClientUtility.checkResult(putTransResponse, uri);
                  String entity = putTransResponse.getEntity(String.class);
                  if (entity != null && !entity.isEmpty())
                  {
                     log.warn("{}", entity);
                  }
               }
            });
         }
      }
   }

   protected void deleteObsoleteDocsFromServer(Set<String> localDocNames)
   {
      ClientResponse<List<ResourceMeta>> getResponse = translationResources.get(null);
      ClientUtility.checkResult(getResponse, uri);
      List<ResourceMeta> remoteDocList = getResponse.getEntity();
      for (ResourceMeta doc : remoteDocList)
      {
         // NB ResourceMeta.name = HDocument.docId
         String docName = doc.getName();
         String docUri = RestUtil.convertToDocumentURIId(docName);
         if (!localDocNames.contains(docName))
         {
            log.info("deleting resource {} from server", docName);
            ClientResponse<String> deleteResponse = translationResources.deleteResource(docUri);
            ClientUtility.checkResult(deleteResponse, uri);
         }
      }
   }

}
