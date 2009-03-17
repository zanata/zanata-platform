package org.fedorahosted.flies.vcs.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.Changeset.Modification;
import org.fedorahosted.flies.vcs.git.GitChangeset;
import org.fedorahosted.flies.vcs.git.GitSandbox;


public class TestGitchangeset extends TestCase{
	private GitSandbox sandbox;
	private Changeset changeset;
	private File root = new File("/home/jamesni/gittutor");
	
	
	public void setUp(){
		sandbox = new GitSandbox(root);
		changeset = sandbox.getCurrentChangeset();
	}
	
	public void testGetChanges(){
		Map<String, Modification> map = changeset.getChanges();
		for (Map.Entry<String, Modification> entry : map.entrySet()) {
			System.out.println(entry.getValue());
			System.out.println(entry.getKey());
			
		}
		
	}
	
	public void testGetChangeswithMod() {
		List<String> filelist = changeset.getChanges(Modification.Add);
		for(String name:filelist) {
			System.out.println("Modify:"+name);
		}
	}
	
	public void testGetChangesetFromOldTo() {
		Changeset oldrev = new GitChangeset("c4866026e0c74997d655da12ad92ad2fa5cc851f", root);
		Changeset rev = new GitChangeset("0be6bbac02d5459abc11011234d7212656c6f965",root);
		List<Changeset> list = sandbox.getChangesets(oldrev, rev);
		System.out.println("TestGetChangesets:\n");
		for(Changeset set: list) {
			System.out.println(set.getId());
			System.out.println(set.getTimestamp());
			System.out.println(set.getUser().getUsername());
			System.out.println(set.getUser().getEmail()+"\n");
		}
	}
	
	public void testGetChangesetFromOld() {
		Changeset oldrev = new GitChangeset("0be6bbac02d5459abc11011234d7212656c6f965", root);
		List<Changeset> list = sandbox.getRevisionsSince(oldrev);
		System.out.println("TestGetChangesetFromOld:\n");
		for(Changeset set: list) {
			System.out.println(set.getId());
			System.out.println(set.getTimestamp());
			System.out.println(set.getUser().getUsername());
			System.out.println(set.getUser().getEmail()+"\n");
		}
	}
}
