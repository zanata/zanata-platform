package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.cyclopsgroup.jcli.ArgumentProcessor;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;
import org.fedorahosted.flies.rest.ClientUtility;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IProjectIterationResource;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;

@Cli(name = "createiter", description = "Creates a project iteration in Flies")
public class CreateIterationTask extends Task implements Subcommand {

	private String user;
	private String apiKey;
	private String fliesURL;
	private boolean debug;
	private boolean help;
	private boolean errors;
	private String proj;
	private String iter;
	private String name;
	private String desc;

	public static void main(String[] args) throws Exception {
		CreateIterationTask task = new CreateIterationTask();
		task.processArgs(args, GlobalOptions.EMPTY);
	}

	@Override
	public void processArgs(String[] args, GlobalOptions globals) throws IOException,
			JAXBException, MalformedURLException, URISyntaxException {
		if (args.length == 0) {
			help(System.out);
			System.exit(0);
		}
		ArgumentProcessor<CreateIterationTask> argProcessor = ArgumentProcessor.newInstance(CreateIterationTask.class);
		argProcessor.process(args, this);
		if (help || globals.getHelp()) {
			help(System.out);
			System.exit(0);
		}
		
		if(globals.getErrors())
			errors = true;
		
		if (fliesURL == null)
			missingOption("--flies");
		if (user == null)
			missingOption("--user");
		if (apiKey == null)
			missingOption("--key");
		if (proj == null)
			missingOption("--proj");
		if (iter == null)
			missingOption("--iter");
		if (name == null)
			missingOption("--name");
		if (desc == null)
			missingOption("--desc");
				
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
		ArgumentProcessor<CreateIterationTask> argProcessor = ArgumentProcessor.newInstance(CreateIterationTask.class);
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
	
	public void process() throws JAXBException, URISyntaxException, IOException {
			JAXBContext jc = JAXBContext.newInstance(Project.class);
			Marshaller m = jc.createMarshaller();
			// debug
			if (debug)
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			ProjectIteration iteration = new ProjectIteration();
			iteration.setId(iter);
			iteration.setName(name);
			iteration.setDescription(desc);
			
			if (debug) {
				m.marshal(iteration, System.out);
			}

			if(fliesURL == null)
				return;
			URL iterURL = new URL(fliesURL+"/seam/resource/restv1/projects/p/"+proj+"/iterations/i/"+iter);
			// send iter to rest api
			FliesClientRequestFactory factory = new FliesClientRequestFactory(user, apiKey);
			IProjectIterationResource iterResource = factory.getProjectIterationResource(iterURL.toURI());
			Response response = iterResource.put(iteration);
			ClientUtility.checkResult(response, iterURL);
	}
	
	@Override
	public void log(String msg) {
		super.log(msg+"\n\n");
	}
	
	@Option(name = "u", longName = "user", required = true, description = "Flies user name")
	public void setUser(String user) {
		this.user = user;
	}

	@Option(name = "k", longName = "key", required = true, description = "Flies API key (from Flies Profile page)")
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Option(name = "f", longName = "flies", required = true, description = "Flies base URL, eg http://flies.example.com/flies")
	public void setFliesURL(String url) {
		this.fliesURL = url;
	}
	
	@Option(name = "p", longName = "proj", required = true, description = "Flies project ID")
	public void setProj(String id) {
		this.proj = id;
	}
	
	@Option(name = "i", longName = "iter", required = true, description = "Flies project iteration ID")
	public void setIter(String id) {
		this.iter = id;
	}
	
	@Option(name = "n", longName = "name", required = true, description = "Flies project iteration name")
	public void setName(String name) {
		this.name = name;
	}
	
	@Option(name = "d", longName = "desc", required = true, description = "Flies project iteration description")
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	@Option(name = "x", longName = "debug", description = "Enable debug mode")
	public void setDebug(boolean debug) {
		this.debug = debug;
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
