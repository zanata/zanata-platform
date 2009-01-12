package org.fedorahosted.flies.vcs.mercurial;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.UserInfo;
import org.fedorahosted.flies.vcs.VcsSandbox;


public class HgSandbox implements VcsSandbox {
	
	private File root;
	private static final Pattern GET_REVISIONS_PATTERN = Pattern
	.compile("^([0-9]+):([a-f0-9]+) ([^ ]+ [^ ]+ [^ ]+) ([^#]+) ([^#]+)");
	
	public HgSandbox(File dest) {
		root = dest;
	}
	
	public HgSandbox(URL source, File dest) {
		cloneCommand(source);
		root = dest;
	}
	
	@Override
	public void commit(List<File> files, UserInfo user, String message) {
		// TODO Auto-generated method stub
        HgCommand command = new HgCommand("commit", root, true);
        command.addUserName(user.getUsername());
        command.addOptions("-m", "add");
        command.addFiles(this.toPaths(files));
        String msg = command.executeToString();
        System.out.println(msg);
        
        HgCommand pushcmd = new HgCommand("push", root, true);
        String msg1 = pushcmd.executeToString();
        System.out.println(msg1);
 	}

	@Override
	public Changeset getCurrentChangeset() {
		// TODO Auto-generated method stub
		Changeset changeSet = null;
		
		HgCommand command = new HgCommand("heads", root, true);
        command.addOptions("--template",
        "{rev}:{node} {date|isodate} {author|person} {author|email}");
		
		String result = command.executeToString();
        Matcher m = GET_REVISIONS_PATTERN.matcher(result);
	    if (m.matches()) {
	    	changeSet = new HgChangeset(
	        m.group(2), // changeset
	        m.group(3), // date
	        m.group(4), // user
	        m.group(5), // email
	        root
	        );
	     } 
   		return changeSet;
	}

	@Override
	public List<Changeset> getChangesets(Changeset from, Changeset to) {
		// TODO Auto-generated method stub
		HgCommand command = new HgCommand("log", root, true);
		command.addOptions("-r",from.getId()+":"+to.getId());
		command.addOptions("--template",
        "{rev}:{node} {date|isodate} {author|person} {author|email}\n");
		String[] lines = null;
		lines = command.executeToString().split("\n");
		 int length = lines.length;
	        List<Changeset> changeSets = new ArrayList<Changeset>();
	        for (int i = 0; i < length; i++) {
	            Matcher m = GET_REVISIONS_PATTERN.matcher(lines[i]);
	            if (m.matches()) {
	                Changeset changeset = new HgChangeset(
	                        m.group(2), // changeset
	                        m.group(3), // date
	                        m.group(4), // user
	                        m.group(5), // email
	                        root
	                        );
	                changeSets.add(changeset);
	                }
	            } 
		return changeSets;
	}

	@Override
	public List<Changeset> getRevisionsSince(Changeset oldRevision) {
		// TODO Auto-generated method stub
		List<Changeset> changesets = new ArrayList<Changeset>();
		Changeset current = getCurrentChangeset();
        changesets = getChangesets(oldRevision, current);		
		return changesets;
	}
	
	public Changeset tipCommand() {
		// TODO Auto-generated method stub
		Changeset changeSet = null;
		
		HgCommand command = new HgCommand("tip", root, true);
        command.addOptions("--template",
        "{rev}:{node} {date|isodate} {author|person} {author|email}");
		
		String result = command.executeToString();
        Matcher m = GET_REVISIONS_PATTERN.matcher(result);
	    if (m.matches()) {
	    	changeSet = new HgChangeset(
	        m.group(2), // changeset
	        m.group(3), // date
	        m.group(4), // user
	        m.group(5), // email
	        root
	        );
	     } 
   		return changeSet;
	}
	
	private void cloneCommand(URL url) {
		HgCommand command = new HgCommand("clone", root, false);
		command.addOptions(url.getPath());
	    command.executeToBytes();
	}
	
	private void pullCommand() {
	    HgCommand command = new HgCommand("pull", root, false);
	    command.executeToBytes();
	}
	
	private void updateCommand() {
		HgCommand command = new HgCommand("update", root, false);
	    command.executeToString();
	}
	
	private void updateCommand(String rev) {
		HgCommand command = new HgCommand("update", root, false);
	    if (rev != null) {
	    	command.addOptions("-r", rev);
	    }
	    command.executeToString();
	}

	@Override
	public boolean isUpdated() {
		// TODO Auto-generated method stub
		pullCommand();
		Changeset tip = tipCommand();
		Changeset parent = getCurrentChangeset();
		if(parent.getId().equals(tip.getId()))
			return true;
		else
			return false;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		pullCommand();
		updateCommand();
	}

	@Override
	public void update(Changeset rev) {
		// TODO Auto-generated method stub
		String revision = rev.getId();
		pullCommand();
		updateCommand(revision);
	}
	
    private List<String> toPaths(List<File> files) {
        List<String> paths = new ArrayList<String>();
        for (File f : files) {
            paths.add(f.getAbsolutePath());
        }
        return paths;
    }

	@Override
	public List<Changeset> getChangesets(String filename, Changeset from,
			Changeset to) {
		// TODO Auto-generated method stub
		HgCommand command = new HgCommand("log", root, true);
		command.addOptions("-r",from.getId()+":"+to.getId());
		command.addOptions("--template",
        "{rev}:{node} {date|isodate} {author|person} {author|email}\n");
		command.addOptions(filename);
		String[] lines = null;
		lines = command.executeToString().split("\n");
		 int length = lines.length;
	        List<Changeset> changeSets = new ArrayList<Changeset>();
	        for (int i = 0; i < length; i++) {
	            Matcher m = GET_REVISIONS_PATTERN.matcher(lines[i]);
	            if (m.matches()) {
	                Changeset changeset = new HgChangeset(
	                        m.group(2), // changeset
	                        m.group(3), // date
	                        m.group(4), // user
	                        m.group(5), // email
	                        root
	                        );
	                changeSets.add(changeset);
	                }
	            } 
		return changeSets;
	}

	@Override
	public List<Changeset> getRevisionsSince(String filename,
			Changeset oldRevision) {
		// TODO Auto-generated method stub
		List<Changeset> changesets = new ArrayList<Changeset>();
		Changeset current = getCurrentChangeset();
        changesets = getChangesets(filename, oldRevision, current);		
		return changesets;
	}

}
