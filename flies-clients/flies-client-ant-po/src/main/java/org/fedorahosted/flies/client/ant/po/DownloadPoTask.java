package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.fedorahosted.flies.adapter.po.PoWriter;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.deprecated.Document;
import org.fedorahosted.flies.rest.dto.deprecated.Documents;
import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;

public class DownloadPoTask extends Task implements Subcommand
{

   private String user;
   private String apiKey;
   private boolean debug;
   private File dstDir;
   private String src;
   private boolean help;
   private boolean errors;
   private boolean exportPot;

   public static void main(String[] args) throws Exception
   {
      DownloadPoTask task = new DownloadPoTask();
      ArgsUtil.processArgs(task, args, GlobalOptions.EMPTY);
   }

   @Override
   public String getCommandName()
   {
      return "downloadpo";
   }

   @Override
   public String getCommandDescription()
   {
      return "Downloads a Publican project's PO/POT files from Flies after translation, to allow document generation";
   }

   @Override
   public void execute() throws BuildException
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         // make sure RESTEasy classes will be found:
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         process();
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   public void process() throws JAXBException, IOException, URISyntaxException
   {
      Unmarshaller m = null;
      if (debug)
      {
         JAXBContext jc = JAXBContext.newInstance(Documents.class);
         m = jc.createUnmarshaller();
      }

      URL srcURL = Utility.createURL(src, Utility.getBaseDir(getProject()));

      List<Document> docList;
      if ("file".equals(srcURL.getProtocol()))
      {
         Documents docs = (Documents) m.unmarshal(new File(srcURL.getFile()));
         docList = docs.getDocuments();
      }
      else
      {
         // use rest api to fetch Documents
         FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
         IDocumentsResource documentsResource = factory.getDocuments(srcURL.toURI());
         ClientResponse<Documents> response = documentsResource.getDocuments();

         ClientUtility.checkResult(response, srcURL.toURI());
         docList = response.getEntity().getDocuments();
      }
      for (Document doc : docList)
      {
         PoWriter pw = new PoWriter();
         pw.write(doc, dstDir, exportPot);
      }
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
   }

   // private void logVerbose(String msg) {
   // super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
   // }

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)", required = true)
   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   @Option(aliases = { "-x" }, name = "--debug", usage = "Enable debug mode")
   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   @Option(aliases = { "-d" }, name = "--dst", metaVar = "DIR", required = true, usage = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
   }

   @Option(name = "--exportpot", usage = "Export source text from Flies to local POT files")
   public void setExportPot(boolean exportPot)
   {
      this.exportPot = exportPot;
   }

   // TODO make --src optional, and provide --flies, --proj, --iter options

   @Option(aliases = { "-s" }, name = "--src", metaVar = "URL", required = true, usage = "Source URL for download, eg http://flies.example.com/seam/resource/restv1/projects/p/myProject/iterations/i/myIter/documents")
   public void setSrc(String src)
   {
      this.src = src;
   }

   @Option(name = "--user", metaVar = "USER", usage = "Flies user name", required = true)
   public void setUser(String user)
   {
      this.user = user;
   }

   @Override
   public boolean getHelp()
   {
      return this.help;
   }

   @Option(name = "--help", aliases = { "-h", "-help" }, usage = "Display this help and exit")
   public void setHelp(boolean help)
   {
      this.help = help;
   }

   @Override
   public boolean getErrors()
   {
      return this.errors;
   }

   @Option(name = "--errors", aliases = { "-e" }, usage = "Output full execution error messages")
   public void setErrors(boolean errors)
   {
      this.errors = errors;
   }

}
