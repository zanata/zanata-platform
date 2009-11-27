package org.fedorahosted.flies.client.ant.po;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.selectors.FileSelector;

class BasePoSelector implements FileSelector {
    private String[] locales;

    public BasePoSelector(String[] locales) {
	this.locales = locales;
    }

    @Override
    public boolean isSelected(File basedir, String filename, File file)
	    throws BuildException {
	for (String loc : locales) {
	    if (filename.endsWith(loc + ".po")) { //$NON-NLS-1$ //$NON-NLS-2$
	    // log("skipping translated property file for now: "+filename);
		return false;
	    }
	}
	return true;
    }
}
