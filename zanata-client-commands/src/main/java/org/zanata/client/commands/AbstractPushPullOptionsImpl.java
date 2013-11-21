/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.kohsuke.args4j.Option;
import org.zanata.client.config.LocaleList;

/**
 * Specifies options that have the same description and defaults for push and
 * pull
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public abstract class AbstractPushPullOptionsImpl<O extends PushPullOptions>
        extends ConfigurableProjectOptionsImpl implements PushPullOptions {
    // FIXME duplicated in ConfigurableProjectCommand
    protected static final String PROJECT_TYPE_FILE = "file";

    private static final String DEF_FROM_DOC = null;
    private static final boolean DEFAULT_DRY_RUN = false;

    protected String[] locales;
    private LocaleList effectiveLocales;
    private File transDir;
    private File srcDir;
    private String fromDoc = DEF_FROM_DOC;
    private boolean dryRun = DEFAULT_DRY_RUN;

    /**
     * Override the parent method as the push and pull commands can have locales
     * specified via command line.
     *
     * @return The locale map list taking into account the global locales in
     *         zanata.xml as well as the command line argument ones.
     */
    @Override
    public LocaleList getLocaleMapList() {
        if (effectiveLocales == null) {
            effectiveLocales =
                    PushPullCommand.getLocaleMapList(super.getLocaleMapList(),
                            locales);
        }
        return effectiveLocales;
    }

    @Override
    public File getSrcDir() {
        return srcDir;
    }

    @Option(
            aliases = { "-s" },
            name = "--src-dir",
            metaVar = "DIR",
            required = true,
            usage = "Base directory for source files (eg \".\", \"pot\", \"src/main/resources\")")
    public
            void setSrcDir(File file) {
        this.srcDir = file;
    }

    @Override
    public String getSrcDirParameterName() {
        return "--src-dir";
    }

    @Override
    public File getTransDir() {
        return transDir;
    }

    @Option(
            aliases = { "-t" },
            name = "--trans-dir",
            metaVar = "DIR",
            required = true,
            usage = "Base directory for translated files (eg \".\", \"po\", \"src/main/resources\")")
    public
            void setTransDir(File transDir) {
        this.transDir = transDir;
    }

    @Override
    public String getFromDoc() {
        return fromDoc;
    }

    @Override
    public String buildFromDocArgument(String argValue) {
        return "--from-doc \"" + argValue + "\"";
    }

    @Option(
            name = "--from-doc",
            metaVar = "DOCID",
            required = false,
            usage = "Specifies a document from which to begin the operation. "
                    + "Documents before this document (sorted alphabetically) will be skipped.\n"
                    + "Use this option to resume a failed operation.")
    public
            void setFromDoc(String fromDoc) {
        this.fromDoc = fromDoc;
    }

    @Override
    public boolean isDryRun() {
        return this.dryRun;
    }

    @Option(
            aliases = { "-n" },
            name = "--dry-run",
            usage = "Dry run: don't change any data, on the server or on the filesystem.")
    public
            void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public boolean getEnableModules() {
        // modules are currently only supported by Maven Mojos:
        return false;
    }

    @Override
    public String getDocNameRegex() {
        // modules are currently only supported by Maven Mojos:
        return null;
    }

    @Override
    public String getModuleSuffix() {
        // modules are currently only supported by Maven Mojos:
        return null;
    }

    @Override
    public boolean isRootModule() {
        return false;
    }

    @Override
    public String getCurrentModule() {
        return "";
    }

    @Override
    public String getCurrentModule(boolean canonicalName) {
        return "";
    }

    @Override
    public Set<String> getAllModules() {
        return Collections.emptySet();
    }

}
