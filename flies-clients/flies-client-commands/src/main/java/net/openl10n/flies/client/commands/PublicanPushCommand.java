package net.openl10n.flies.client.commands;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.adapter.po.PoReader2;
import net.openl10n.flies.client.commands.gettext.PublicanUtil;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;
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

   private File srcDir;

   private String sourceLang = "en-US";

   private boolean importPo;

   private boolean validate;

   public PublicanPushCommand() throws JAXBException
   {
      super();
   }

   @Override
   public String getCommandName()
   {
      return "publican-push";
   }

   @Override
   public String getCommandDescription()
   {
      return "Publishes publican source text to a Flies project version so that it can be translated.";
   }

   @Option(aliases = { "-s" }, name = "--src", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
   }

   @Option(aliases = { "-l" }, name = "--src-lang", usage = "Language of source (defaults to en-US)")
   public void setSourceLang(String sourceLang)
   {
      this.sourceLang = sourceLang;
   }

   @Option(name = "--import-po", usage = "Import translations from local PO files to Flies, overwriting or erasing existing translations (DANGER!)")
   public void setImportPo(boolean importPo)
   {
      this.importPo = importPo;
   }

   @Option(name = "--validate", usage = "Validate XML before sending request to server")
   public void setValidate(boolean validate)
   {
      this.validate = validate;
   }


   @Override
   public void run() throws Exception
   {
      if (getUrl() == null)
         throw new Exception("Flies URL must be specified");
      if (getProject() == null)
         throw new Exception("Project must be specified");
      if (getProjectVersion() == null)
         throw new Exception("Project version must be specified");
      System.out.println("Flies server: " + getUrl());
      System.out.println("Project: " + getProject());
      System.out.println("Version: " + getProjectVersion());
      // System.out.println("List of resources:");

      // NB we don't load all the docs into a HashMap, because that would waste
      // memory
      Set<String> localDocNames = new HashSet<String>();
      // populate localDocNames by looking in pot directory
      File potDir = new File(srcDir, "pot");
      File[] potFiles = PublicanUtil.findPotFiles(potDir);
      for (File pot : potFiles)
      {
         String potName = pot.getName();
         String docName = StringUtil.removeFileExtension(potName, ".pot");
         localDocNames.add(docName);
      }

      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      ITranslationResources translationResources = factory.getTranslationResources(getProject(), getProjectVersion());
      ClientResponse<List<ResourceMeta>> response = translationResources.get();
      ClientUtility.checkResult(response, factory.getTranslationResourcesURI(getProject(), getProjectVersion()));
      List<ResourceMeta> remoteList = response.getEntity();
      for (ResourceMeta doc : remoteList)
      {
         // NB ResourceMeta.name = HDocument.docId
         if (!localDocNames.contains(doc.getName()))
         {
            translationResources.deleteResource(doc.getName());
         }
      }
      File[] localeDirs = null;
      if (importPo)
      {
         localeDirs = PublicanUtil.findLocaleDirs(srcDir);
      }

      PoReader2 poReader = new PoReader2();
      for (String docId : localDocNames)
      {
         Resource srcDoc = new Resource(docId);
         File potFile = new File(potDir, docId + ".pot");
         InputSource potInputSource = new InputSource(potFile.toURI().toString());
         // load 'srcDoc' from pot/${docID}.pot
         poReader.extractTemplate(srcDoc, potInputSource, new LocaleId(sourceLang));
         translationResources.putResource(docId, srcDoc);
         if (importPo)
         {
            for (File localeDir : localeDirs)
            {
               File poFile = new File(localeDir, docId + ".po");
               if (poFile.exists())
               {
                  InputSource inputSource = new InputSource(poFile.toURI().toString());
                  inputSource.setEncoding("utf8");
                  String publicanLocale = localeDir.getName();
                  // TODO locale mapping
                  LocaleId locale = new LocaleId(publicanLocale);
                  TranslationsResource targetDoc = new TranslationsResource();
                  poReader.extractTarget(srcDoc, targetDoc, inputSource, locale);
                  translationResources.putTranslations(docId, locale, targetDoc);
               }
            }
         }
      }
   }

}
