package org.fedorahosted.flies.vcs.git;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.UserInfo;
import org.fedorahosted.flies.vcs.VcsSandbox;
import org.fedorahosted.flies.vcs.git.GitCommand;


public class GitSandbox implements VcsSandbox{
	private File root;
	
	public GitSandbox(File rootpath) {
		// TODO Auto-generated constructor stub
		root = rootpath;
	}
	
	public GitSandbox(URL source, File dest) {
		cloneCommand(source);
		root = dest;
	}

	@Override
	public void commit(List<File> files, UserInfo user, String message) {
		// TODO Auto-generated method stub
        GitCommand pushcmd = new GitCommand("push", root, true);
        String msg1 = pushcmd.executeToString();
        System.out.println(msg1);
	}

	@Override
	public Changeset getCurrentChangeset() {
		// TODO Auto-generated method stub
		Changeset changeSet = null;
		GitCommand command = new GitCommand("log", root, true);
		command.addOptions("-1");
		command.addOptions("HEAD");
		command.addOptions("--pretty=format:%H:%an:%ae:%ai");
		String result = command.executeToString();
		String[] items = result.split(":",4);
	    changeSet = new GitChangeset(items[0],items[1],items[2],items[3],root);
	    return changeSet;
	}

	private String getChangesetParentId(Changeset from) {
		// TODO Auto-generated method stub
		GitCommand command = new GitCommand("log", root, true);
		command.addOptions("-1", "--pretty=format:%P");
		command.addOptions(from.getId());
		return command.executeToString();
	}
	
	@Override
	public List<Changeset> getChangesets(Changeset from, Changeset to) {
		// TODO Auto-generated method stub
		GitCommand command = new GitCommand("log", root, true);
		String parentId = getChangesetParentId(from);
		if(!parentId.equals("")) {
			command.addOptions(parentId+".."+to.getId());
		} else {
			command.addOptions(to.getId());
		}
		command.addOptions("--pretty=format:%H:%an:%ae:%ai");
		String[] lines = command.executeToString().split("\n");
		List<Changeset> changeSets = new ArrayList<Changeset>();
	    for(String line: lines)
	    {
	    	String[] items = line.split(":",4);
	    	Changeset changeSet = new GitChangeset(items[0],items[1],items[2],items[3],root);
	    	changeSets.add(changeSet);
	    }
		return changeSets;
	}

	@Override
	public List<Changeset> getRevisionsSince(Changeset oldRevision) {
		// TODO Auto-generated method stub
		GitCommand command = new GitCommand("log", root, true);
		String parentId = getChangesetParentId(oldRevision);
		if(!parentId.equals("")) {
			command.addOptions(parentId+"..");
		} else {
			command.addOptions("HEAD");
		}
		command.addOptions("--pretty=format:%H:%an:%ae:%ai");
		String[] lines = command.executeToString().split("\n");
		List<Changeset> changeSets = new ArrayList<Changeset>();
	    for(String line: lines)
	    {
	    	String[] items = line.split(":",4);
	    	Changeset changeSet = new GitChangeset(items[0],items[1],items[2],items[3],root);
	    	changeSets.add(changeSet);
	    }
		return changeSets;
	}

	@Override
	public boolean isUpdated() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void update() {
		// TODO Auto-generated method stub
	    GitCommand command = new GitCommand("pull", root, false);
	    command.executeToBytes();
	}

	@Override
	public void update(Changeset rev) {
		// TODO Auto-generated method stub
	    GitCommand command = new GitCommand("pull", root, false);
	    command.addOptions(rev.getId());
	    command.executeToBytes();
	}

	@Override
	public List<Changeset> getChangesets(String filename, Changeset from,
			Changeset to) {
		// TODO Auto-generated method stub
		GitCommand command = new GitCommand("log", root, true);
		String parentId = getChangesetParentId(from);
		if(!parentId.equals("")) {
			command.addOptions(parentId+".."+to.getId());
		} else {
			command.addOptions(to.getId());
		}
		command.addOptions("--pretty=format:%H:%an:%ae:%ai");
		command.addOptions(filename);
		String[] lines = command.executeToString().split("\n");
		List<Changeset> changeSets = new ArrayList<Changeset>();
	    for(String line: lines)
	    {
	    	String[] items = line.split(":",4);
	    	Changeset changeSet = new GitChangeset(items[0],items[1],items[2],items[3],root);
	    	changeSets.add(changeSet);
	    }
		return changeSets;
	}

	@Override
	public List<Changeset> getRevisionsSince(String filename,
			Changeset oldRevision) {
		// TODO Auto-generated method stub
		GitCommand command = new GitCommand("log", root, true);
		String parentId = getChangesetParentId(oldRevision);
		if(!parentId.equals("")) {
			command.addOptions(parentId+"..");
		} else {
			command.addOptions("HEAD");
		}
		command.addOptions("--pretty=format:%H:%an:%ae:%ai");
		command.addOptions(filename);
		String[] lines = command.executeToString().split("\n");
		List<Changeset> changeSets = new ArrayList<Changeset>();
	    for(String line: lines)
	    {
	    	String[] items = line.split(":",4);
	    	Changeset changeSet = new GitChangeset(items[0],items[1],items[2],items[3],root);
	    	changeSets.add(changeSet);
	    }
		return changeSets;
	}
	
	private void cloneCommand(URL url) {
		GitCommand command = new GitCommand("clone", root, false);
		command.addOptions(url.getPath());
	    command.executeToBytes();
	}

}
