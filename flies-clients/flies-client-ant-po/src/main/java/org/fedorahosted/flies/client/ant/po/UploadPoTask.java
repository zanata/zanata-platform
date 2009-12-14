package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.fedorahosted.flies.adapter.po.PoReader;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.ClientUtility;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.jboss.resteasy.client.ClientResponse;
import org.xml.sax.InputSource;

public class UploadPoTask extends MatchingTask {

	private String user;
	private String apiKey;
	private String dst;
	private File srcDir;
	private String sourceLang = "en_US";
	private boolean debug;
	private ContentState contentState = ContentState.Approved;
	
	File file;
	public void setup() throws IOException{
		file = File.createTempFile("poReaderTests", ".xml");
		System.out.println("creating file: " + file);
		if(file.exists())
			file.delete();
	}
	
	public void execute() throws BuildException, NoSuchElementException {
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
//		Document doc = new Document("doc1","mydoc.doc", "/", PoReader.PO_CONTENT_TYPE);
		
		try {
			// make sure RESTEasy classes will be found:
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			
//			InputSource inputSource = new InputSource("file:/home/cchance/src/fedorahosted/flies/flies-client/flies-client-ant-po/src/test/resources/test-input/ja-JP/Accounts_And_Subscriptions.po");
//			
//			inputSource.setEncoding("utf8");
//			
			PoReader poReader = new PoReader();
//
//			System.out.println("parsing template");
//			poReader.extractTemplate(doc, inputSource);
			
			// scan the directory for pot files
			DirectoryScanner ds = getDirectoryScanner(srcDir);
			if (!getImplicitFileSet().hasPatterns())
				ds.setIncludes(new String[] { "pot/*.pot" }); //$NON-NLS-1$
			ds.scan();
			String[] potFilenames = ds.getIncludedFiles();
			
			// debug: print scanned files
			if (debug) {
				System.out.println("Here are scanned files: ");
				for (String potFilename : potFilenames)
					System.out.println(potFilename);
			}

			JAXBContext jc = JAXBContext.newInstance(Documents.class);
			Marshaller m = jc.createMarshaller();
			
			// debug
			if (debug)
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			Documents docs = new Documents();
			List<Document> docList = docs.getDocuments();
			
			File[] localeDirs = srcDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() && !f.getName().equals("pot");
				}
			});
			
			// for each of the base pot files under srcdir/pot:
			for (String potFilename : potFilenames) {
//				progress.update(i++, files.length);
				File potFile = new File(srcDir, potFilename);
//				File sourceLang = new File(srcDir, locale);
				Document doc = new Document(potFilename, ContentType.TextPlain);
				InputSource potInputSource = new InputSource(potFile.toURI().toString());
				System.out.println(potFile.toURI().toString());
				potInputSource.setEncoding("utf8");
				poReader.extractTemplate(doc, potInputSource, LocaleId.fromJavaName(sourceLang));
				docList.add(doc);
				
				String basename = StringUtil.removeFileExtension(potFile.getName(), ".pot");
				String poName = basename + ".po";
				
				// for each of the corresponding po files in the locale subdirs:
				for (int i = 0; i < localeDirs.length; i++) {
					File localeDir = localeDirs[i];
					File poFile = new File(localeDir, poName);
					if (poFile.exists()) {
	//					progress.update(i++, files.length);
						InputSource inputSource = new InputSource(poFile.toURI().toString());
						System.out.println(poFile.toURI().toString());
						inputSource.setEncoding("utf8");
						poReader.extractTarget(doc, inputSource, new LocaleId(localeDir.getName()));
						docList.add(doc);
					}
				}
			}		
//			progress.finished();
			
			if (debug) {
				m.marshal(docs, System.out);
			}

			if(dst == null)
				return;

			// check if local or remote: write to file if local, put to server if remote
			URL dstURL = Utility.createURL(dst, getProject());
			if("file".equals(dstURL.getProtocol())) {
				m.marshal(docs, new File(dstURL.getFile()));
			} else {
				// send project to rest api
				FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
				IDocumentsResource documentsResource = factory.getDocumentsResource(dstURL.toURI());
				ClientResponse response = documentsResource.put(docs);
				ClientUtility.checkResult(response, dstURL);
			}

		} catch (Exception e) {
			throw new BuildException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}
	
	@Override
	public void log(String msg) {
		super.log(msg+"\n\n");
	}
	
	public void setUser(String user) {
		this.user = user;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}

	public void setSrcDir(File srcDir) {
		this.srcDir = srcDir;
	}

	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
