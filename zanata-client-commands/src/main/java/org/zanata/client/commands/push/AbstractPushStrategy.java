/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;

/**
 * NB: you must initialise this object with init() after setPushOptions()
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public abstract class AbstractPushStrategy extends
        AbstractCommonPushStrategy<PushOptions> {
    private StringSet extensions;
    private String fileExtension;

    public abstract Set<String> findDocNames(File srcDir,
            ImmutableList<String> includes, ImmutableList<String> excludes,
            boolean useDefaultExclude, boolean caseSensitive,
            boolean excludeLocaleFilenames) throws IOException;

    public abstract Resource loadSrcDoc(File sourceDir, String docName)
            throws IOException;

    public abstract void visitTranslationResources(String docName,
            Resource srcDoc, TranslationResourcesVisitor visitor)
            throws IOException;

    public AbstractPushStrategy(StringSet extensions, String fileExtension) {
        this.extensions = extensions;
        this.fileExtension = fileExtension;
    }

    /**
     * Indicates if this strategy must work without access to source files. No
     * attempt should be made to read, write or push source documents for a
     * trans-only strategy.
     *
     * @return true if this strategy only allows interactions with translation
     *         files.
     */
    public boolean isTransOnly() {
        return false;
    }

    /**
     * Scan srcDir to return a list of all source files.
     *
     * @param srcDir
     *            base directory in which to find source files
     * @param includes
     *            empty to find all source files, non-empty to find only the
     *            documents in this list
     * @param excludes
     *            'excludes' patterns configured by the user
     * @param excludeLocaleFilenames
     *            adds entries to excludes to ignore any file with a locale id
     *            suffix before the file extension.
     * @param useDefaultExclude
     *            true to also exclude a set of default excludes for common temp
     *            file and source control filenames
     * @param isCaseSensitive
     *            case sensitive search for includes and excludes options
     * @return document paths for source files found in srcDir
     */
    public String[] getSrcFiles(File srcDir, ImmutableList<String> includes,
            ImmutableList<String> excludes, boolean excludeLocaleFilenames,
            boolean useDefaultExclude, boolean isCaseSensitive) {
        ImmutableList<String> fullExcludes =
                getFullExcludes(excludes, excludeLocaleFilenames);
        return getSrcFiles(srcDir, includes, fullExcludes,
                ImmutableList.of(fileExtension),
                useDefaultExclude, isCaseSensitive);
    }

    private ImmutableList<String> getFullExcludes(ImmutableList<String> excludes,
            boolean excludeLocaleFilenames) {
        if (excludeLocaleFilenames) {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            // excludes may not be mutable, so make a copy
            builder.addAll(excludes);
            builder.addAll(getExcludesForLocaleFilenames());
            return builder.build();
        } else {
            return excludes;
        }
    }

    private List<String> getExcludesForLocaleFilenames() {
        List<String> excludes = new ArrayList<String>();
        String sourceLang =
                new LocaleId(getOpts().getSourceLang()).toJavaName();

        for (LocaleMapping locMap : getOpts().getLocaleMapList()) {
            String loc = locMap.getJavaLocale();
            if (!sourceLang.equals(loc)) {
                excludes.add("**/*_" + loc + fileExtension);
            }
        }
        return excludes;
    }

    protected String docNameToFilename(String docName) {
        return docName + fileExtension;
    }

    protected String docNameToFilename(String docName, LocaleMapping locale) {
        return docName + "_" + locale.getJavaLocale() + fileExtension;
    }

    public StringSet getExtensions() {
        return extensions;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void init() {
    }

}
