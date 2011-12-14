package org.zanata.client.commands.push;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
   private static final String UTF_8 = "UTF-8";

   private static final Map<String, AbstractPushStrategy> strategies = new HashMap<String, AbstractPushStrategy>();

   public static interface TranslationResourcesVisitor
   {
      void visit(LocaleMapping locale, TranslationsResource targetDoc);
   }

   {
      strategies.put(PROJECT_TYPE_UTF8_PROPERTIES, new PropertiesStrategy(UTF_8));
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

   private void logOptions()
   {
      if (!log.isInfoEnabled())
      {
         return;
      }
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      log.info("Project type: {}", getOpts().getProjectType());
      log.info("Source language: {}", getOpts().getSourceLang());
      log.info("Copy previous translations: {}", getOpts().getCopyTrans());
      log.info("Merge type: {}", getOpts().getMergeType());
      log.info("Enable modules: {}", getOpts().getEnableModules());
      if (getOpts().getEnableModules())
      {
         log.info("Current module: {}", getOpts().getCurrentModule());
         if (getOpts().isRootModule())
         {
            log.info("Root module: YES");
            if (log.isDebugEnabled())
            {
               log.debug("Modules: {}", StringUtils.join(getOpts().getAllModules(), ", "));
            }
         }
      }
      log.info("Include patterns: {}", StringUtils.join(getOpts().getIncludes(), " "));
      log.info("Exclude patterns: {}", StringUtils.join(getOpts().getExcludes(), " "));
      log.info("Default excludes: {}", getOpts().getDefaultExcludes());

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
      if (getOpts().isDryRun())
      {
         log.info("DRY RUN: no permanent changes will be made");
      }
   }

   @Override
   public void run() throws Exception
   {
      logOptions();
      pushCurrentModule();

      if (getOpts().getEnableModules() && getOpts().isRootModule())
      {
         List<String> obsoleteDocs = getObsoleteDocNamesForProjectIterationFromServer();
         log.info("found {} docs in obsolete modules (or no module): {}", obsoleteDocs.size(), obsoleteDocs);
         if (getOpts().getDeleteObsoleteModules() && !obsoleteDocs.isEmpty())
         {
            // offer to delete obsolete documents
            confirmWithUser("Do you want to delete all documents from the server which don't belong to any module in the Maven reactor?\n");
            deleteDocsFromServer(obsoleteDocs);
         }
         else
         {
            log.warn("found {} docs in obsolete modules (or no module).  use -Dzanata.deleteObsoleteModules to delete them", obsoleteDocs.size());
         }
      }
   }

   /**
    * gets doc list from server,
    * returns a list of qualified doc names from obsolete modules, or from no module.
    */
   protected List<String> getObsoleteDocNamesForProjectIterationFromServer()
   {
      if (!getOpts().getEnableModules())
         return Collections.emptyList();
      List<ResourceMeta> remoteDocList = getDocListForProjectIterationFromServer();

      Pattern p = Pattern.compile(getOpts().getDocNameRegex());
      Set<String> modules = new HashSet<String>(getOpts().getAllModules());

      List<String> obsoleteDocs = new ArrayList<String>();
      for (ResourceMeta doc : remoteDocList)
      {
         // NB ResourceMeta.name == HDocument.docId
         String docName = doc.getName();

         Matcher matcher = p.matcher(docName);
         if (matcher.matches())
         {
            String module = matcher.group(1);
            if (modules.contains(module))
            {
               log.debug("doc {} belongs to non-obsolete module {}", docName, module);
            }
            else
            {
               obsoleteDocs.add(docName);
               log.info("doc {} belongs to obsolete module {}", docName, module);
            }
         }
         else
         {
            obsoleteDocs.add(docName);
            log.warn("doc {} doesn't belong to any module", docName);
         }
      }
      return obsoleteDocs;
   }

   private void pushCurrentModule() throws IOException
   {
      File sourceDir = getOpts().getSrcDir();

      if (!sourceDir.exists())
      {
         if (getOpts().getEnableModules())
         {
            log.info("source directory '" + sourceDir + "' not found; skipping docs push for module " + getOpts().getCurrentModule());
            return;
         }
         else
         {
            throw new RuntimeException("directory '" + sourceDir + "' does not exist - check srcDir option");
         }
      }

      AbstractPushStrategy strat = getStrategy(getOpts().getProjectType());
      final StringSet extensions = strat.getExtensions();

      // to save memory, we don't load all the docs into a HashMap
      Set<String> localDocNames = strat.findDocNames(sourceDir, getOpts().getIncludes(), getOpts().getExcludes(), getOpts().getDefaultExcludes());
      for (String docName : localDocNames)
      {
         log.info("Found source document: {}", docName);
      }
      List<String> obsoleteDocs = getObsoleteDocsInModuleFromServer(localDocNames);
      if (obsoleteDocs.isEmpty())
      {
         if (localDocNames.isEmpty())
         {
            log.info("no documents in module: {}; nothing to do", getOpts().getCurrentModule());
            return;
         }
         else
         {
            // nop
         }
      }
      else
      {
         log.warn("Found {} obsolete docs on the server which will be DELETED");
         log.info("Obsolete docs: {}", obsoleteDocs);
      }

      if (getOpts().getPushTrans())
      {
         if (getOpts().getLocales() == null)
            throw new ConfigException("pushTrans option set, but zanata.xml contains no <locales>");
         log.warn("pushTrans option is set: existing translations on server may be overwritten/deleted");
         confirmWithUser("This will overwrite existing documents AND TRANSLATIONS on the server, and delete obsolete documents.\n");
      }
      else
      {
         confirmWithUser("This will overwrite existing documents on the server, and delete obsolete documents.\n");
      }

      for (String localDocName : localDocNames)
      {
         final Resource srcDoc = strat.loadSrcDoc(sourceDir, localDocName);
         String qualifiedDocName = qualifiedDocName(localDocName);
         final String docUri = RestUtil.convertToDocumentURIId(qualifiedDocName);
         srcDoc.setName(qualifiedDocName);
         debug(srcDoc);

         pushSrcDocToServer(docUri, srcDoc, extensions);

         if (getOpts().getPushTrans())
         {
            strat.visitTranslationResources(localDocName, srcDoc, new TranslationResourcesVisitor()
            {
               @Override
               public void visit(LocaleMapping locale, TranslationsResource targetDoc)
               {
                  debug(targetDoc);
                  pushTargetDocToServer(docUri, locale, srcDoc, targetDoc, extensions);
               }
            });
         }
      }
      deleteDocsFromServer(obsoleteDocs);
   }

   /**
    * Returns obsolete docs which belong to the current module.
    * Returns any docs in the current module from the server, unless they are found in the localDocNames set.
    * @param localDocNames
    */
   private List<String> getObsoleteDocsInModuleFromServer(Set<String> localDocNames)
   {
      List<String> qualifiedDocNames = getQualifiedDocNamesForCurrentModuleFromServer();
      List<String> obsoleteDocs = new ArrayList<String>(qualifiedDocNames.size());
      for (String qualifiedDocName : qualifiedDocNames)
      {
         String unqualifiedDocName = unqualifiedDocName(qualifiedDocName);
         if (!localDocNames.contains(unqualifiedDocName))
         {
            obsoleteDocs.add(qualifiedDocName);
         }
      }
      return obsoleteDocs;
   }

   /**
    * @param qualifiedDocNames
    */
   private void deleteDocsFromServer(List<String> qualifiedDocNames)
   {
      for (String qualifiedDocName : qualifiedDocNames)
      {
         deleteDocFromServer(qualifiedDocName);
      }
   }

   private void pushSrcDocToServer(final String docUri, final Resource srcDoc, final StringSet extensions)
   {
      if (!getOpts().isDryRun())
      {
         log.info("pushing source document [name={}] to server", srcDoc.getName());
         boolean copyTrans = getOpts().getCopyTrans();
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions, copyTrans);
         ClientUtility.checkResult(putResponse, uri);
      }
      else
      {
         log.info("pushing source document [name={}] to server (skipped due to dry run)", srcDoc.getName());
      }
   }

   private void pushTargetDocToServer(final String docUri, LocaleMapping locale, final Resource srcDoc, TranslationsResource targetDoc, final StringSet extensions)
   {
      if (!getOpts().isDryRun())
      {
         log.info("pushing target document [name={} client-locale={}] to server [locale={}]", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
         ClientResponse<String> putTransResponse = translationResources.putTranslations(docUri, new LocaleId(locale.getLocale()), targetDoc, extensions, getOpts().getMergeType());
         ClientUtility.checkResult(putTransResponse, uri);
         String entity = putTransResponse.getEntity(String.class);
         if (entity != null && !entity.isEmpty())
         {
            log.warn("{}", entity);
         }
      }
      else
      {
         log.info("pushing target document [name={} client-locale={}] to server [locale={}] (skipped due to dry run)", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
      }
   }

   private void deleteDocFromServer(String qualifiedDocName)
   {
      if (!getOpts().isDryRun())
      {
         log.info("deleting resource {} from server", qualifiedDocName);
         String docUri = RestUtil.convertToDocumentURIId(qualifiedDocName);
         ClientResponse<String> deleteResponse = translationResources.deleteResource(docUri);
         ClientUtility.checkResult(deleteResponse, uri);
      }
      else
      {
         log.info("deleting resource {} from server (skipped due to dry run)", qualifiedDocName);
      }
   }

   private List<String> getQualifiedDocNamesForCurrentModuleFromServer()
   {
      List<ResourceMeta> remoteDocList = getDocListForProjectIterationFromServer();
      List<String> docNames = new ArrayList<String>();
      for (ResourceMeta doc : remoteDocList)
      {
         // NB ResourceMeta.name = HDocument.docId
         String qualifiedDocName = doc.getName();
         if (getOpts().getEnableModules())
         {
            if (belongsToCurrentModule(qualifiedDocName))
            {
               docNames.add(qualifiedDocName);
            }
            else
            {
               log.debug("found extra-modular document: {}", qualifiedDocName);
            }
         }
         else
         {
            docNames.add(qualifiedDocName);
         }
      }
      return docNames;
   }

}
