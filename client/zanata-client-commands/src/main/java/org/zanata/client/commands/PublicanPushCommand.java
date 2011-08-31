package org.zanata.client.commands;

import java.io.BufferedInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.adapter.po.PoReader2;
import org.zanata.client.commands.gettext.PublicanUtil;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.JaxbUtil;
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
 * @deprecated
 * @see org.zanata.client.commands.push.PushCommand
 */
public class PublicanPushCommand extends ConfigurableProjectCommand<PublicanPushOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PublicanPushCommand.class);

   private final ITranslationResources translationResources;
   private final URI uri;

   public PublicanPushCommand(PublicanPushOptions opts, ZanataProxyFactory factory, ITranslationResources translationResources, URI uri)
   {
      super(opts, factory);
      this.translationResources = translationResources;
      this.uri = uri;
   }

   private PublicanPushCommand(PublicanPushOptions opts, ZanataProxyFactory factory)
   {
      this(opts, factory, factory.getTranslationResources(opts.getProj(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PublicanPushCommand(PublicanPushOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   protected String getProjectType()
   {
      return PROJECT_TYPE_PUBLICAN;
   }

   @Override
   public void run() throws Exception
   {
      log.info("Server: {}", getOpts().getUrl());
      log.info("Project: {}", getOpts().getProj());
      log.info("Version: {}", getOpts().getProjectVersion());
      log.info("Username: {}", getOpts().getUsername());
      log.info("Source language: {}", getOpts().getSourceLang());
      log.info("Copy previous translations: {}", getOpts().getCopyTrans());
      log.info("Merge type: {}", getOpts().getMergeType());
      if (getOpts().getImportPo())
      {
         log.info("Importing source and target documents");
      }
      else
      {
         log.info("Importing source documents only");
      }
      log.info("POT directory (originals): {}", getOpts().getSrcDirPot());
      if (getOpts().getImportPo())
      {
         log.info("PO base directory (translations): {}", getOpts().getSrcDir());
      }
      File potDir = getOpts().getSrcDirPot();

      if (!potDir.exists())
      {
         throw new RuntimeException("directory '" + potDir + "' does not exist - check srcDir and srcDirPot options");
      }

      Console console = System.console();
      if (getOpts().isInteractiveMode())
      {
         if (console == null)
            throw new RuntimeException("console not available: please run maven from a console, or use batch mode (mvn -B)");
      }

      if (getOpts().getImportPo())
      {
         log.warn("importPo option is set: existing translations on server will be overwritten/deleted");
         if (getOpts().isInteractiveMode())
         {
            console.printf("This will overwrite/delete any existing documents AND TRANSLATIONS on the server.\n");
            console.printf("Are you sure (y/n)? ");
            expectYes(console);
         }
      }
      else if (getOpts().isInteractiveMode())
      {
         console.printf("This will overwrite/delete any existing documents on the server.\n");
         console.printf("Are you sure (y/n)? ");
         expectYes(console);
      }

      JAXBContext jc = null;
      if (log.isDebugEnabled() || getOpts().getValidate())
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

      String[] potFiles = PublicanUtil.findPotFiles(potDir, new AndFileFilter());
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
      if (getOpts().getImportPo())
      {
         if (getOpts().getLocales() != null)
         {
            locales = PublicanUtil.findLocales(getOpts().getSrcDir(), getOpts().getLocales());
            if (locales.size() == 0)
            {
               log.warn("option 'importPo' is set, but none of the configured locale directories was found (check zanata.xml)");
            }
         }
         else
         {
            locales = PublicanUtil.findLocales(getOpts().getSrcDir());
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
         Resource srcDoc;
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(potFile));
         try
         {
            InputSource potInputSource = new InputSource(bis);
            potInputSource.setEncoding("utf8");
            // load 'srcDoc' from pot/${docID}.pot
            srcDoc = poReader.extractTemplate(potInputSource, new LocaleId(getOpts().getSourceLang()), docName);
         }
         finally
         {
            bis.close();
         }
         if (log.isDebugEnabled())
         {
            StringWriter writer = new StringWriter();
            m.marshal(srcDoc, writer);
            log.debug("{}", writer);
         }
         if (getOpts().getValidate())
         {
            JaxbUtil.validateXml(srcDoc, jc);
         }
         StringSet extensions = new StringSet("comment;gettext");
         log.info("pushing source document [name={}] to server", srcDoc.getName());
         boolean copyTrans = getOpts().getCopyTrans();
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions, copyTrans );
         ClientUtility.checkResult(putResponse, uri);

         if (getOpts().getImportPo())
         {
            for (LocaleMapping locale : locales)
            {
               File localeDir = new File(getOpts().getSrcDir(), locale.getLocalLocale());
               File poFile = new File(localeDir, docName + ".po");
               if (poFile.canRead())
               {
                  TranslationsResource targetDoc;
                  BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(poFile));
                  try
                  {
                     InputSource inputSource = new InputSource(bis2);
                     inputSource.setEncoding("utf8");
                     // NB we always use source order in this impl
                     targetDoc = poReader.extractTarget(inputSource, srcDoc, true);
                  }
                  finally
                  {
                     bis2.close();
                  }
                  if (log.isDebugEnabled())
                  {
                     StringWriter writer = new StringWriter();
                     m.marshal(targetDoc, writer);
                     log.debug("{}", writer);
                  }
                  if (getOpts().getValidate())
                  {
                     JaxbUtil.validateXml(targetDoc, jc);
                  }
                  log.info("pushing target document [name={} client-locale={}] to server [locale={}]", new Object[] { srcDoc.getName(), locale.getLocalLocale(), locale.getLocale() });
                  ClientResponse<String> putTransResponse = translationResources.putTranslations(docUri, new LocaleId(locale.getLocale()), targetDoc, extensions, getOpts().getMergeType());
                  ClientUtility.checkResult(putTransResponse, uri);
                  String entity = putTransResponse.getEntity(String.class);
                  if (entity != null && !entity.isEmpty())
                  {
                     log.warn("{}", entity);
                  }
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
