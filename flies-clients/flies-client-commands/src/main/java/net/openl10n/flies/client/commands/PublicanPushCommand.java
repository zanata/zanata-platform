package net.openl10n.flies.client.commands;

import java.io.File;
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
      log.debug("Flies server: {}", opts.getUrl());
      log.debug("Project: {}", opts.getProj());
      log.debug("Version: {}", opts.getProjectVersion());
      // log.debug("List of resources:");
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
      File potDir = new File(opts.getSrcDir(), "pot");

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
         ClientResponse<String> putResponse = translationResources.putResource(docUri, srcDoc, extensions);
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

}
