/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.client.commands.push;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;

/**
 * Strategy that provides basic directory scanning for source files.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public abstract class AbstractCommonPushStrategy<O extends PushOptions> {

    private O opts;

    /**
     * @return the Options object associated with this strategy.
     */
    public O getOpts() {
        return opts;
    }

    public void setPushOptions(O opts) {
        this.opts = opts;
    }

    /**
     * excludes should already contain paths for translation files that are to
     * be excluded.
     */
    public String[] getSrcFiles(File srcDir, List<String> includes,
            List<String> excludes, List<String> fileExtensions,
            boolean useDefaultExcludes, boolean isCaseSensitive) {
        if (includes.isEmpty()) {
            for (String fileExtension : fileExtensions) {
                includes.add("**/*" + fileExtension);
            }
        }

        DirectoryScanner dirScanner = new DirectoryScanner();

        if (useDefaultExcludes) {
            dirScanner.addDefaultExcludes();
        }

        dirScanner.setBasedir(srcDir);

        dirScanner.setCaseSensitive(isCaseSensitive);

        dirScanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        dirScanner.setIncludes(includes.toArray(new String[includes.size()]));
        dirScanner.scan();
        String[] includedFiles = dirScanner.getIncludedFiles();
        for (int i = 0; i < includedFiles.length; i++) {
            // canonicalise file separator (to handle backslash on Windows)
            includedFiles[i] = includedFiles[i].replace(File.separator, "/");
        }
        return includedFiles;
    }

}
