package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.cyclopsgroup.jcli.ArgumentProcessor;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;
import org.fedorahosted.flies.adapter.po.PoWriter;
import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.jboss.resteasy.client.ClientResponse;

@Cli(name = "downloadpo", description = "Downloads a Publican project's PO/POT files from Flies after translation, to allow document generation")
public class DownloadPoTask extends Task implements Subcommand {

	private String user;
	private String apiKey;
	private boolean debug;
	private File dstDir;
	private String src;
	private boolean help;
	private boolean errors;
	private boolean exportPot;

	public static void main(String[] args) throws Exception {
		DownloadPoTask download = new DownloadPoTask();
		download.processArgs(args, GlobalOptions.EMPTY);
	}

	@Override
	public void processArgs(String[] args, GlobalOptions globals)
			throws IOException, JAXBException, URISyntaxException {
		if (args.length == 0) {
			help(System.out);
			System.exit(0);
		}
		ArgumentProcessor<DownloadPoTask> argProcessor = ArgumentProcessor.newInstance(DownloadPoTask.class);
		argProcessor.process(args, this);
		if (help || globals.getHelp()) {
			help(System.out);
			System.exit(0);
		}
		
		if(globals.getErrors())
			errors = true;
		
		if (src == null)
			missingOption("--src");
		if (dstDir == null)
			missingOption("--dst");
		if (user == null)
			missingOption("--user");
		if (apiKey == null)
			missingOption("--key");
				
		try {
			process();
		} catch (Exception e) {
			Utility.handleException(e, errors);
		}
	}
	
	private static void missingOption(String name) {
		System.out.println("Required option missing: "+name);
		System.exit(1);
	}
	
	public static void help(PrintStream output) throws IOException {
		ArgumentProcessor<DownloadPoTask> argProcessor = ArgumentProcessor.newInstance(DownloadPoTask.class);
		PrintWriter out = new PrintWriter(output);
		argProcessor.printHelp(out);
		out.flush();
	}
	
	@Override
	public void execute() throws BuildException {
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try {
			// make sure RESTEasy classes will be found:
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			process();
		} catch (Exception e) {
			throw new BuildException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}
	
	public void process() throws JAXBException, IOException, URISyntaxException {
			Unmarshaller m = null;
			if (debug) {
				JAXBContext jc = JAXBContext.newInstance(Documents.class);
				m = jc.createUnmarshaller();
			}

			URL srcURL = Utility.createURL(src, Utility.getBaseDir(getProject()));

			List<Document> docList;
			if("file".equals(srcURL.getProtocol())) {
				Documents docs = (Documents) m.unmarshal(new File(srcURL.getFile()));
				docList = docs.getDocuments();
			} else {
				// use rest api to fetch Documents
				FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
				IDocumentsResource documentsResource = factory.getDocumentsResource(srcURL.toURI());
				ClientResponse<Documents> response  = documentsResource.getDocuments();

				ClientUtility.checkResult(response, srcURL);
				docList = response.getEntity().getDocuments();
			}
			for (Document doc : docList) {
				PoWriter pw = new PoWriter();
				pw.write(doc, dstDir, exportPot);
			}
	}
	
	@Override
	public void log(String msg) {
		super.log(msg+"\n\n");
	}

	//    private void logVerbose(String msg) {
	//	super.log(msg, org.apache.tools.ant.Project.MSG_VERBOSE);
	//    }

	@Option(name = "k", longName = "key", required = true, description = "Flies API key (from Flies Profile page)")
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Option(name = "x", longName = "debug", description = "Enable debug mode")
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Option(name = "d", longName = "dst", required = true, description = "Base directory for publican files (with subdirectory \"pot\" and optional locale directories)")
	public void setDstDir(File dstDir) {
		this.dstDir = dstDir;
	}

	@Option(name = "e", longName = "exportpot", description = "Export source text from Flies to local POT files")
	public void setExportPot(boolean exportPot) {
		this.exportPot = exportPot;
	}
	
	// TODO make --src optional, and provide --flies, --proj, --iter options

	@Option(name = "s", longName = "src", required = true, description = "Source URL for download, eg http://flies.example.com/seam/resource/restv1/projects/p/myProject/iterations/i/myIter/documents")
	public void setSrc(String src) {
		this.src = src;
	}

	@Option(name = "u", longName = "user", required = true, description = "Flies user name")
	public void setUser(String user) {
		this.user = user;
	}

	@Option(name = "h", longName = "help", description = "Display this help and exit")
	public void setHelp(boolean help) {
		this.help = help;
	}

	@Option(name = "e", longName = "errors", description = "Output full execution error messages")
	public void setErrors(boolean exceptionTrace) {
		this.errors = exceptionTrace;
	}
}
