package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fedorahosted.flies.adapter.po.PoWriter;
import org.fedorahosted.flies.client.commands.ArgsUtil;
import org.fedorahosted.flies.client.commands.BasicOptions;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.deprecated.Document;
import org.fedorahosted.flies.rest.dto.deprecated.Documents;
import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.Option;

public class DownloadPoTask extends FliesTask
{

   private String user;
   private String apiKey;
   private File dstDir;
   private String src;
   private boolean exportPot;

   public static void main(String[] args) throws Exception
   {
      DownloadPoTask task = new DownloadPoTask();
      ArgsUtil.processArgs(task, args, BasicOptions.EMPTY);
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

   public void run() throws JAXBException, IOException, URISyntaxException
   {
      Unmarshaller m = null;
      URL srcURL = Utility.createURL(src, Utility.getBaseDir(getProject()));
      if (getDebug() || "file".equals(srcURL.getProtocol()))
      {
         JAXBContext jc = JAXBContext.newInstance(Documents.class);
         m = jc.createUnmarshaller();
      }

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

   @Option(name = "--key", metaVar = "KEY", usage = "Flies API key (from Flies Profile page)", required = true)
   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
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

}
