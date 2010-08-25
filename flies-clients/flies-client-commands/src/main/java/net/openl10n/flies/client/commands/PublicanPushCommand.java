package net.openl10n.flies.client.commands;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.client.commands.gettext.PublicanUtil;
import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;

import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      for (String docId : localDocNames)
      {
         ResourceMeta doc = null;
         // TODO load 'doc' from pot/${docID}.pot
         File pot = new File(potDir, docId + ".pot");
         translationResources.putResourceMeta(docId, doc);
         if (importPo)
         {
            for (File loc : localeDirs)
            {
               String publicanLocale = loc.getName();
            }
         }
      }
   }

}
