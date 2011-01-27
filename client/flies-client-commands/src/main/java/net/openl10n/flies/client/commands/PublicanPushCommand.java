package net.openl10n.flies.client.commands;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.adapter.po.PoReader2;
import net.openl10n.flies.client.commands.gettext.PublicanUtil;
import net.openl10n.flies.client.config.LocaleMapping;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.JaxbUtil;
import net.openl10n.flies.rest.RestUtil;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class PublicanPushCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(PublicanPushCommand.class);

   private final PublicanPushOptions opts;
   private final ITranslationResources translationResources;
   private final URI uri;


   public PublicanPushCommand(PublicanPushOptions opts, FliesClientRequestFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.opts = opts;
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PublicanPushCommand(PublicanPushOptions opts, FliesClientRequestFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PublicanPushCommand(PublicanPushOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   public void run() throws Exception
   {
      log.info("Flies server: {}", opts.getUrl());
      log.info("Project: {}", opts.getProj());
      log.info("Version: {}", opts.getProjectVersion());
      log.info("Username: {}", opts.getUsername());
      log.info("Source language: {}", opts.getSourceLang());
      log.info("Copy previous translations: {}", opts.getCopyTrans());
      if (opts.getImportPo())
      {
         log.info("Importing source and target documents");
      }
      else
      {
         log.info("Importing source documents only");
      }
      log.info("POT directory (originals): {}", opts.getSrcDirPot());
      if (opts.getImportPo())
      {
         log.info("PO base directory (translations): {}", opts.getSrcDir());
      }
      File potDir = opts.getSrcDirPot();

      if (!potDir.exists())
      {
         throw new RuntimeException("directory '" + potDir + "' does not exist - check srcDir and srcDirPot options");
      }

      Console console = System.console();
      if (opts.isInteractiveMode())
      {
         if (console == null)
            throw new RuntimeException("console not available: please run maven from a console, or use batch mode (mvn -B)");
      }

      if (opts.getImportPo())
      {
         log.warn("importPo option is set: existing translations on server will be overwritten/deleted");
         if (opts.isInteractiveMode())
         {
            console.printf("This will overwrite/delete any existing documents AND TRANSLATIONS on the server.\n");
            console.printf("Are you sure (y/n)? ");
            expectYes(console);
         }
      }
      else if (opts.isInteractiveMode())
      {
         console.printf("This will overwrite/delete any existing documents on the server.\n");
         console.printf("Are you sure (y/n)? ");
         expectYes(console);
      }

      JAXBContext jc = null;
      if (log.isDebugEnabled() || opts.getValidate())
      {
         jc = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
      }
      Marshaller m = null;
      if (log.isDebugEnabled())
      {
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }

      // NB we don't load all the docs into a HashMap, because that would waste
      // memory
      Set<String> localDocNames = new HashSet<String>();
      // populate localDocNames by looking in pot directory
      String[] potFiles = PublicanUtil.findPotFiles(potDir);
      for (String potName : potFiles)
      {
         String docName = StringUtil.removeFileExtension(potName, ".pot");
         localDocNames.add(docName);
      }

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
      List<LocaleMapping> locales = null;
      if (opts.getImportPo())
      {
         if (opts.getLocales() != null)
         {
            locales = PublicanUtil.findLocales(opts.getSrcDir(), opts.getLocales());
            if (locales.size() == 0)
            {
               log.warn("option 'importPo' is set, but none of the configured locale directories was found (check flies.xml)");
            }
         }
         else
         {
            locales = PublicanUtil.findLocales(opts.getSrcDir());
            if (locales.size() == 0)
            {
               log.warn("option 'importPo' is set, but no locale directories were found");
            }
            else
            {
               log.info("option 'importPo' is set, but no locales specified in configuration: importing " + locales.size() + " directories");
            }
         }
      }

      PoReader2 poReader = new PoReader2();
      for (String docName : localDocNames)
      {
         String docUri = RestUtil.convertToDocumentURIId(docName);
         File potFile = new File(potDir, docName + ".pot");
         InputSource potInputSource = new InputSource(potFile.toURI().toString());
         // load 'srcDoc' from pot/${docID}.pot
         Resource srcDoc = poReader.extractTemplate(potInputSource, new LocaleId(opts.getSourceLang()), docName);

         if (log.isDebugEnabled())
         {
            StringWriter writer = new StringWriter();
            m.marshal(srcDoc, writer);
            log.debug("{}", writer);
         }
         if (opts.getValidate())
         {
            JaxbUtil.validateXml(srcDoc, jc);
         }
         StringSet extensions = new StringSet("comment;gettext");
         log.info("pushing source document [name={}] to server", srcDoc.getName());
         boolean copyTrans = opts.getCopyTrans();
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions, copyTrans );
         ClientUtility.checkResult(putResponse, uri);

         if (opts.getImportPo())
         {
            for (LocaleMapping locale : locales)
            {
               File localeDir = new File(opts.getSrcDir(), locale.getLocalLocale());
               File poFile = new File(localeDir, docName + ".po");
               if (poFile.exists())
               {
                  InputSource inputSource = new InputSource(poFile.toURI().toString());
                  inputSource.setEncoding("utf8");
                  TranslationsResource targetDoc = poReader.extractTarget(inputSource, srcDoc);
                  if (log.isDebugEnabled())
                  {
                     StringWriter writer = new StringWriter();
                     m.marshal(targetDoc, writer);
                     log.debug("{}", writer);
                  }
                  if (opts.getValidate())
                  {
                     JaxbUtil.validateXml(targetDoc, jc);
                  }
                  log.info("pushing target document [name={} client-locale={}] to server [locale={}]", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
                  ClientResponse<String> putTransResponse = translationResources.putTranslations(docUri, new LocaleId(locale.getLocale()), targetDoc, extensions);
                  ClientUtility.checkResult(putTransResponse, uri);
               }
            }
         }
      }
   }

   protected void expectYes(Console console) throws IOException
   {
      String line = console.readLine();
      if (line == null)
         throw new IOException("console stream closed");
      if (!line.toLowerCase().equals("y") && !line.toLowerCase().equals("yes"))
         throw new RuntimeException("operation aborted by user");
   }

}
