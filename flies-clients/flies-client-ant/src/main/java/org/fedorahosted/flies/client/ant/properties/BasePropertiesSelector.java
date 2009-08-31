package org.fedorahosted.flies.client.ant.properties;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.selectors.FileSelector;

class BasePropertiesSelector implements FileSelector {
    private String[] locales;

    public BasePropertiesSelector(String[] locales) {
	this.locales = locales;
    }

    @Override
    public boolean isSelected(File basedir, String filename, File file)
	    throws BuildException {
	for (String loc : locales) {
	    if (filename.endsWith("_" + loc + ".properties")) { //$NON-NLS-1$ //$NON-NLS-2$
	    // log("skipping translated property file for now: "+filename);
		return false;
	    }
	}
	return true;
    }
}
