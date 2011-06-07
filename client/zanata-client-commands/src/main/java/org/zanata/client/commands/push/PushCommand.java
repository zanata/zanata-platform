package org.zanata.client.commands.push;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableProjectCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.config.LocaleMapping;
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
public class PushCommand extends ConfigurableProjectCommand
{
   static final Logger log = LoggerFactory.getLogger(PushCommand.class);

   private static final Map<String, PushStrategy> strategies = new HashMap<String, PushStrategy>();

   static interface TranslationResourcesVisitor
   {
      void visit(LocaleMapping locale, TranslationsResource targetDoc);
   }

   {
      // strategies.put("properties", new PropertiesStrategy());
      strategies.put("gettextDir", new GettextDirStrategy());
   }

   Marshaller m = null;

   private final PushOptions opts;
   private final ITranslationResources translationResources;
   private final URI uri;

   public PushCommand(PushOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.opts = opts;
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PushCommand(PushOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PushCommand(PushOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", opts.getUrl());
      log.info("Project: {}", opts.getProj());
      log.info("Version: {}", opts.getProjectVersion());
      log.info("Username: {}", opts.getUsername());
      log.info("Project type: {}", opts.getProjectType());
      log.info("Source language: {}", opts.getSourceLang());
      log.info("Copy previous translations: {}", opts.getCopyTrans());
      log.info("Merge type: {}", opts.getMergeType());
      if (opts.getPushTrans())
      {
         log.info("Pushing source and target documents");
      }
      else
      {
         log.info("Pushing source documents only");
      }
      log.info("Source directory (originals): {}", opts.getSourceDir());
      if (opts.getPushTrans())
      {
         log.info("Target base directory (translations): {}", opts.getTransDir());
      }
      File sourceDir = opts.getSourceDir();

      if (!sourceDir.exists())
      {
         throw new RuntimeException("directory '" + sourceDir + "' does not exist - check sourceDir option");
      }

      if (opts.getPushTrans())
      {
         log.warn("pushTrans option is set: existing translations on server may be overwritten/deleted");
         confirmWithUser("This will overwrite/delete any existing documents AND TRANSLATIONS on the server.\n");
      }
      else
      {
         confirmWithUser("This will overwrite/delete any existing documents on the server.\n");
      }

      PushStrategy strat = strategies.get(opts.getProjectType());
      if (strat == null)
      {
         throw new RuntimeException("unknown project type: " + opts.getProjectType());
      }

      JAXBContext jc = null;
      if (opts.isDebugSet()) // || opts.getValidate())
      {
         jc = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
      }
      if (opts.isDebugSet())
      {
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }

      // NB we don't load all the docs into a HashMap, because that would waste
      // memory
      Set<String> localDocNames = strat.findDocNames(sourceDir);
      deleteObsoleteDocsFromServer(localDocNames);

      for (String docName : localDocNames)
      {
         final String docUri = RestUtil.convertToDocumentURIId(docName);
         final Resource srcDoc = strat.loadSrcDoc(sourceDir, docName);
         debug(srcDoc);
         // if (opts.getValidate())
         // {
         // JaxbUtil.validateXml(srcDoc, jc);
         // }

         final StringSet extensions = strat.getExtensions();
         log.info("pushing source document [name={}] to server", srcDoc.getName());
         boolean copyTrans = opts.getCopyTrans();
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions, copyTrans );
         ClientUtility.checkResult(putResponse, uri);

         if (opts.getPushTrans())
         {
            strat.visitTranslationResources(docUri, docName, srcDoc, new TranslationResourcesVisitor()
            {
               @Override
               public void visit(LocaleMapping locale, TranslationsResource targetDoc)
               {
                  debug(targetDoc);
                  // if (opts.getValidate())
                  // {
                  // JaxbUtil.validateXml(targetDoc, jc);
                  // }
                  log.info("pushing target document [name={} client-locale={}] to server [locale={}]", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
                  ClientResponse<String> putTransResponse = translationResources.putTranslations(docUri, new LocaleId(locale.getLocale()), targetDoc, extensions, opts.getMergeType());
                  ClientUtility.checkResult(putTransResponse, uri);
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

   private void confirmWithUser(String message) throws IOException
   {
      if (opts.isInteractiveMode())
      {
         Console console = System.console();
         if (console == null)
            throw new RuntimeException("console not available: please run Maven from a console, or use batch mode (mvn -B)");
         console.printf(message + "\nAre you sure (y/n)? ");
         expectYes(console);
      }
   }

   protected static void expectYes(Console console) throws IOException
   {
      String line = console.readLine();
      if (line == null)
         throw new IOException("console stream closed");
      if (!line.toLowerCase().equals("y") && !line.toLowerCase().equals("yes"))
         throw new RuntimeException("operation aborted by user");
   }

   protected void debug(Object jaxbElement)
   {
      try
      {
         if (opts.isDebugSet())
         {
            StringWriter writer = new StringWriter();
            m.marshal(jaxbElement, writer);
            log.debug("{}", writer);
         }
      }
      catch (JAXBException e)
      {
         log.debug(e.toString(), e);
      }
   }

}
