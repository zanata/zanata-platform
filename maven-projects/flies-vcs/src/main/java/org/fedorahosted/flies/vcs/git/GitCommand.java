package org.fedorahosted.flies.vcs.git;

import java.io.File;
import java.util.List;

import org.fedorahosted.flies.vcs.AbstractShellCommand;

public class GitCommand extends AbstractShellCommand {
	String gitCommand = "/usr/bin/git";
	
    public GitCommand(List<String> commands, File workingDir, boolean escapeFiles) {
        super(commands, workingDir, escapeFiles);
    }

    public GitCommand(String command, File workingDir, boolean escapeFiles) {
        this.command = command;
        this.workingDir = workingDir;
        this.escapeFiles = escapeFiles;
    }

    protected GitCommand(String command, boolean escapeFiles) {
        this(command, (File) null, escapeFiles);
    }

    protected void addUserName(String user) {
        this.options.add("-u");
        this.options.add(user);  
    }

    @Override
    protected String getExecutable() {
    	return gitCommand;
    }
}
