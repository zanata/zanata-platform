package org.fedorahosted.flies.vcs.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.Changeset.Modification;
import org.fedorahosted.flies.vcs.mercurial.HgChangeset;
import org.fedorahosted.flies.vcs.mercurial.HgSandbox;
import junit.framework.TestCase;


public class TestHgChangeset extends TestCase{
	private HgSandbox sandbox;
	private Changeset changeset;
	private File root = new File("/home/jamesni/workspace/vcslibrary/test");
	
	
	public void setUp(){
		sandbox = new HgSandbox(root);
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
		List<String> filelist = changeset.getChanges(Modification.Modify);
		for(String name:filelist) {
			System.out.println("Modify:"+name);
		}
	}
	
	public void testGetChangesetFromOld() {
		Changeset oldrev = new HgChangeset("dd1fe218886bdcc3df25dd8583fa54faee2626d9", root);
		List<Changeset> list = sandbox.getRevisionsSince(oldrev);
		for(Changeset set: list) {
			System.out.println(set.getId());
			System.out.println(set.getTimestamp());
			System.out.println(set.getUser().getUsername());
			System.out.println(set.getUser().getEmail());
		}
	}
	


}
