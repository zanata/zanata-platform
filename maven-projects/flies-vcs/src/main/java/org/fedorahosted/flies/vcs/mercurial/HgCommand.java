package org.fedorahosted.flies.vcs.mercurial;

import java.io.File;
import java.util.List;
import org.fedorahosted.flies.vcs.AbstractShellCommand;

public class HgCommand extends AbstractShellCommand {
	String hgCommand = "/usr/bin/hg";
	
    public HgCommand(List<String> commands, File workingDir, boolean escapeFiles) {
        super(commands, workingDir, escapeFiles);
    }

    public HgCommand(String command, File workingDir, boolean escapeFiles) {
        this.command = command;
        this.workingDir = workingDir;
        this.escapeFiles = escapeFiles;
    }

    protected HgCommand(String command, boolean escapeFiles) {
        this(command, (File) null, escapeFiles);
    }

    protected void addUserName(String user) {
        this.options.add("-u");
        this.options.add(user);  
    }

    @Override
    protected String getExecutable() {
    	return hgCommand;
    }
}
