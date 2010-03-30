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
public class PoTool {

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
		if (help || arguments.isEmpty()) {
			help(System.out);
			System.exit(0);
		}
		String command = arguments.get(0);
		final String[] otherArgs = arguments.subList(1, arguments.size()).toArray(new String[0]);
		if (command.equals("upload")) {
			UploadPoTask.main(otherArgs);
		} else if (command.equals("download")) {
			DownloadPoTask.main(otherArgs);
		} else {
			help(System.out);
		}
	}
	
	private static void help(PrintStream out) throws IOException {
		out.println("[USAGE]"); 
		out.println("  flies-publican upload/download [--help] [options] [args]");
		out.println();
	}
	
	private boolean help;
	@Option(name = "h", longName = "help", description = "Display this help and exit")
	public void setHelp(boolean help) {
		this.help = help;
	}

	private List<String> arguments = new ArrayList<String>();
	 
	@MultiValue
	@Argument( description = "Arguments" )
	public List<String> getArguments() { return arguments; }
	
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
