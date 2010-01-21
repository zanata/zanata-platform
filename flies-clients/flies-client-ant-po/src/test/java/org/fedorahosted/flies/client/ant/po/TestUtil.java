package org.fedorahosted.flies.client.ant.po;

import java.io.File;

public class TestUtil {
    public static void delete(File d) {
	if (d.isDirectory()) {
	    for (File f : d.listFiles()) {
		delete(f);
	    }
	}
	d.delete();
    }
}
