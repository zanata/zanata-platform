package org.fedorahosted.flies.vcs.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.fedorahosted.flies.vcs.mercurial.HgSandbox;
import org.fedorahosted.flies.vcs.mercurial.HgUserInfo;

public class TestHgCommit extends TestCase{
	private HgSandbox sandbox;
	private HgUserInfo hguser;
	private File root = new File("/home/jamesni/workspace/vcslibrary/test");
	
	public void setUp(){
		sandbox = new HgSandbox(root);
		hguser = new HgUserInfo("jni", "");
	}
		
	public void testCommit(){
		File file = new File("test/sample.txt");
		List<File> files = new ArrayList<File>();
		files.add(file);
		sandbox.commit(files, hguser, "");
	}

}
