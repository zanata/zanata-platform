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
      this(opts, factory, factory.getTranslationResources(opts.getProject(), opts.getProjectVersion()), factory.getTranslationResourcesURI(opts.getProject(), opts.getProjectVersion()));
   }

   public PublicanPushCommand(PublicanPushOptions opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   @Override
   public void run() throws Exception
   {
      log.debug("Flies server: {}", opts.getUrl());
      log.debug("Project: {}", opts.getProject());
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
      File[] potFiles = PublicanUtil.findPotFiles(potDir);
      for (File pot : potFiles)
      {
         String potName = pot.getName();
         String docName = StringUtil.removeFileExtension(potName, ".pot");
         localDocNames.add(docName);
      }

      ClientResponse<List<ResourceMeta>> response = translationResources.get(null);
      ClientUtility.checkResult(response, uri);
      List<ResourceMeta> remoteList = response.getEntity();
      for (ResourceMeta doc : remoteList)
      {
         // NB ResourceMeta.name = HDocument.docId
         if (!localDocNames.contains(doc.getName()))
         {
            translationResources.deleteResource(doc.getName());
         }
      }
      List<LocaleMapping> locales = null;
      if (opts.getImportPo())
      {
         if (opts.getLocales() != null)
            locales = PublicanUtil.findLocales(opts.getSrcDir(), opts.getLocales());
         else
            locales = PublicanUtil.findLocales(opts.getSrcDir());
      }

      PoReader2 poReader = new PoReader2();
      for (String docId : localDocNames)
      {
         File potFile = new File(potDir, docId + ".pot");
         InputSource potInputSource = new InputSource(potFile.toURI().toString());
         // load 'srcDoc' from pot/${docID}.pot
         Resource srcDoc = poReader.extractTemplate(potInputSource, new LocaleId(opts.getSourceLang()), docId);

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
         translationResources.putResource(docId, srcDoc, extensions);
         if (opts.getImportPo())
         {
            for (LocaleMapping locale : locales)
            {
               File localeDir = new File(opts.getSrcDir(), locale.getLocalLocale());
               File poFile = new File(localeDir, docId + ".po");
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
                  translationResources.putTranslations(docId, new LocaleId(locale.getLocale()), targetDoc, extensions);
               }
            }
         }
      }
   }

}
