package org.fedorahosted.flies.client.ant.po;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.cyclopsgroup.jcli.ArgumentProcessor;
import org.cyclopsgroup.jcli.annotation.Argument;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.MultiValue;
import org.cyclopsgroup.jcli.annotation.Option;

@Cli(name = "flies-publican", description = "Send publican PO/POT files to and from Flies")
public class PoTool implements GlobalOptions {

	private boolean help;
	private boolean errors;
	private boolean version;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PoTool tool = new PoTool();
		ArgumentProcessor<PoTool> argProcessor = ArgumentProcessor.newInstance(PoTool.class);
		argProcessor.process(args, tool);
		tool.processArgs();
	}
	
	private void processArgs() throws Exception {
		if (version) {
			Utility.printJarVersion(System.out);
			System.exit(0);
		}
		if (help || arguments.isEmpty()) {
			help(System.out);
			System.exit(0);
		}
		String command = arguments.get(0);
		String[] otherArgs = arguments.subList(1, arguments.size()).toArray(new String[0]);
			try {
			if (command.equals("upload")) {
				Subcommand upload = new UploadPoTask();
				upload.processArgs(otherArgs, this);
			} else if (command.equals("download")) {
				Subcommand download = new DownloadPoTask();
				download.processArgs(otherArgs, this);
			} else {
				help(System.out);
			}
		} catch (Exception e) {
			Utility.handleException(e, errors);
		}
	}

	private static void help(PrintStream out) throws IOException {
		out.println("[USAGE]"); 
		out.println("  flies-publican [-e/--errors] upload/download [--help] [options] [args]");
		out.println();
	}
	
	@Override
	public boolean getHelp() {
		return help;
	}
	
	@Option(name = "h", longName = "help", description = "Display this help and exit")
	public void setHelp(boolean help) {
		this.help = help;
	}

	@Override
	public boolean getErrors() {
		return errors;
	}
	
	@Option(name = "e", longName = "errors", description = "Output full execution error messages")
	public void setErrors(boolean exceptionTrace) {
		this.errors = exceptionTrace;
	}

	@Option(name = "v", longName = "version", description = "Output version information and exit")
	public void setVersion(boolean version) {
		this.version = version;
	}
	
	private List<String> arguments = new ArrayList<String>();
	 
	@MultiValue
	@Argument( description = "Arguments" )
	public List<String> getArguments() { return arguments; }
	
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
