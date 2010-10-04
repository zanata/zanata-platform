package net.openl10n.flies.client.ant.properties;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.adapter.properties.PropReader;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.IDocumentsResource;
import net.openl10n.flies.rest.dto.VersionInfo;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.Documents;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Props2DocsTask extends BaseTask
{
   private static final Logger log = LoggerFactory.getLogger(Props2DocsTask.class);

   private String user;
   private String apiKey;
   private boolean debug;
   private String dst;
   private String[] locales;
   private String sourceLang;
   private File srcDir;
   private ContentState contentState = ContentState.Approved;

   @Override
   public void execute() throws BuildException
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         // make sure RESTEasy classes will be found:
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         DirectoryScanner ds = getDirectoryScanner(srcDir);
         // use default includes if unset:
         if (!getImplicitFileSet().hasPatterns())
         {
            ds.setIncludes(new String[] { "**/*.properties" }); //$NON-NLS-1$
         }
         ds.setSelectors(getSelectors());
         ds.scan();
         String[] files = ds.getIncludedFiles();

         Marshaller m = null;
         JAXBContext jc = JAXBContext.newInstance(Documents.class);
         m = jc.createMarshaller();
         if (debug)
         {
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         }

         Documents docs = new Documents();
         List<Document> docList = docs.getDocuments();
         PropReader propReader = new PropReader();
         // for each of the base props files under srcdir:
         int i = 0;
         for (String filename : files)
         {
            progress.update(i++, files.length);
            Document doc = new Document(filename, ContentType.TextPlain);
            doc.setLang(LocaleId.fromJavaName(sourceLang));
            File f = new File(srcDir, filename);
            propReader.extractAll(doc, f, locales, contentState);
            docList.add(doc);
         }
         progress.finished();
         if (debug)
         {
            StringWriter writer = new StringWriter();
            m.marshal(docs, writer);
            log.debug("{}", writer);
         }

         if (dst == null)
            return;

         URL dstURL = Utility.createURL(dst, getProject());
         if ("file".equals(dstURL.getProtocol()))
         {
            m.marshal(docs, new File(dstURL.getFile()));
         }
         else
         {
            // send project to rest api
            FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey, new VersionInfo("SNAPSHOT", "Unknow"));
            IDocumentsResource documentsResource = factory.getDocuments(dstURL.toURI());
            ClientResponse response = documentsResource.put(docs);
            ClientUtility.checkResult(response, dstURL.toURI());
         }

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

   FileSelector[] getSelectors()
   {
      if (locales != null)
         return new FileSelector[] { new BasePropertiesSelector(locales) };
      else
         return new FileSelector[0];
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
   }

   private void logVerbose(String msg)
   {
      super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
   }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   public void setContentState(String contentState)
   {
      this.contentState = ContentState.valueOf(contentState);
   }

   public void setDebug(boolean debug)
   {
      this.debug = debug;
   }

   public void setDst(String dst)
   {
      this.dst = dst;
   }

   public void setLocales(String locales)
   {
      this.locales = locales.split(","); //$NON-NLS-1$
   }

   public void setSourceLang(String sourceLang)
   {
      this.sourceLang = sourceLang;
   }

   public void setSrcDir(File srcDir)
   {
      this.srcDir = srcDir;
      logVerbose("srcDir=" + srcDir);
   }

   public void setUser(String user)
   {
      this.user = user;
   }

}
