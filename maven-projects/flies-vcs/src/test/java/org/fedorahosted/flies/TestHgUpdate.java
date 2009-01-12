package org.fedorahosted.flies.vcs.test;

import java.io.File;
import junit.framework.TestCase;

import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.mercurial.HgSandbox;

public class TestHgUpdate extends TestCase{
	private HgSandbox sandbox;
	private Changeset hgrev;
	private File root = new File("/home/jamesni/workspace/vcslibrary/test");
	
	public void setUp(){
		sandbox = new HgSandbox(root);
		hgrev = sandbox.getCurrentChangeset();
	}
	
	public void testUpdateWithRevision(){
		sandbox.update(hgrev);
	}
	
	public void testUpdate(){
		sandbox.update();
	}



}
