package net.openl10n.flies.client.ant.properties;

import java.io.File;

import org.apache.tools.ant.BuildException;

public class Docs2PropsTask extends BaseTask
{

   private String user;
   private String apiKey;
   private boolean debug;
   private File dstDir;
   private String src;
   private boolean exportRoot;

   @Override
   public void execute() throws BuildException
   {
      // ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      // try
      // {
      // // make sure RESTEasy classes will be found:
      // Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      // Unmarshaller m = null;
      // if (debug)
      // {
      // JAXBContext jc = JAXBContext.newInstance(Documents.class);
      // m = jc.createUnmarshaller();
      // }
      //
      // URL srcURL = Utility.createURL(src, getProject());
      //
      // List<Document> docList;
      // if ("file".equals(srcURL.getProtocol()))
      // {
      // Documents docs = (Documents) m.unmarshal(new File(srcURL.getFile()));
      // docList = docs.getDocuments();
      // }
      // else
      // {
      // // use rest api to fetch Documents
      // FliesClientRequestFactory factory = new FliesClientRequestFactory(user,
      // apiKey, new VersionInfo("SNAPSHOT", "Unknow"));
      // IDocumentsResource documentsResource =
      // factory.getDocuments(srcURL.toURI());
      // ClientResponse<Documents> response = documentsResource.getDocuments();
      //
      // ClientUtility.checkResult(response, srcURL.toURI());
      // docList = response.getEntity().getDocuments();
      // }
      //
      // for (Document doc : docList)
      // {
      // PropWriter.write(doc, dstDir, exportRoot);
      // }
      // }
      // catch (Exception e)
      // {
      // throw new BuildException(e);
      // }
      // finally
      // {
      // Thread.currentThread().setContextClassLoader(oldLoader);
      // }
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
   }

   // private void logVerbose(String msg) {
   // super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
   // }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   public void setDstDir(File dstDir)
   {
      this.dstDir = dstDir;
   }

   public void setSrc(String src)
   {
      this.src = src;
   }

   public void setUser(String user)
   {
      this.user = user;
   }

   public void setExportRootLocale(boolean exportRoot)
   {
      this.exportRoot = exportRoot;
   }

   public boolean isExportRoot()
   {
      return exportRoot;
   }

   public void setExportRoot(boolean exportRoot)
   {
      this.exportRoot = exportRoot;
   }

   public String getUser()
   {
      return user;
   }

   public String getApiKey()
   {
      return apiKey;
   }

   public boolean isDebug()
   {
      return debug;
   }

   public File getDstDir()
   {
      return dstDir;
   }

   public String getSrc()
   {
      return src;
   }

}
