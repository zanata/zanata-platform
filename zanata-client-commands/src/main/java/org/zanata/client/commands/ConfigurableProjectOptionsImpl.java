/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;
import org.zanata.client.config.LocaleList;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Base options for commands which supports configuration by the user's
 * zanata.ini and by a project's zanata.xml
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public abstract class ConfigurableProjectOptionsImpl extends
        ConfigurableOptionsImpl implements ConfigurableProjectOptions {

    /**
     * Project configuration file for Zanata client.
     */
    // When used as a CLI command, the default path (specified here) is relative
    // to CWD. ConfigurableProjectMojo specifies another default, which is
    // relative to project's basedir.
    private File projectConfig = new File("zanata.xml");

    private String project;
    private String projectVersion;
    private String projectType;
    private LocaleList locales;
    private File transDir;
    private File srcDir;
    private ImmutableList<String> includes = ImmutableList.of();
    private ImmutableList<String> excludes = ImmutableList.of();
    private Splitter splitter = Splitter.on(",").omitEmptyStrings()
            .trimResults();

    @Override
    public String getProj() {
        return project;
    }

    @Override
    @Option(
            name = "--project",
            metaVar = "PROJ",
            usage = "Project ID.  This value is required unless specified in zanata.xml.")
    public
            void setProj(String projectSlug) {
        this.project = projectSlug;
    }

    @Override
    @Option(name = "--project-config", metaVar = "FILENAME",
            usage = "Project configuration file, eg zanata.xml\n"
                    + "Default is zanata.xml in the current directory.",
            required = false)
    public void setProjectConfig(File projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    @Override
    @Option(
            name = "--project-version",
            metaVar = "VER",
            usage = "Project version ID.  This value is required unless specified in zanata.xml.")
    public
            void setProjectVersion(String versionSlug) {
        this.projectVersion = versionSlug;
    }

    @Override
    public String getProjectType() {
        return projectType;
    }

    @Override
    @Option(
            name = "--project-type",
            metaVar = "PROJTYPE",
            usage = "Type of project:\n"
                    + "    \"properties\" = Java .properties,\n"
                    + "    \"podir\" = publican-style gettext directories,\n"
                    + "    \"utf8properties\" = UTF-8 .properties files,\n"
                    + "    \"gettext\" = gettext PO files,\n"
                    + "    \"file\" = EXPERIMENTAL document files of various types).\n"
                    + "If 'file' is used, transDir must not be the same as or nested within srcDir,\n"
                    + "and vice versa.")
    public
            void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    @Override
    public File getProjectConfig() {
        return projectConfig;
    }

    @Override
    public LocaleList getLocaleMapList() {
        return locales;
    }

    @Override
    public void setLocaleMapList(LocaleList locales) {
        this.locales = locales;
    }

    @Option(
            aliases = { "-s" },
            name = "--src-dir",
            metaVar = "DIR",
            usage = "Base directory for source files (eg \".\", \"pot\", \"src/main/resources\")")
    @Override
    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    @Override
    public File getSrcDir() {
        return srcDir;
    }

    @Option(
            aliases = { "-t" },
            name = "--trans-dir",
            metaVar = "DIR",
            usage = "Base directory for translated files (eg \".\", \"po\", \"src/main/resources\")")
    @Override
    public void setTransDir(File transDir) {
        this.transDir = transDir;
    }

    @Override
    public File getTransDir() {
        return transDir;
    }

    @Override
    public ImmutableList<String> getIncludes() {
        return includes;
    }

    @Option(
            name = "--includes",
            metaVar = "INCLUDES",
            usage = "Wildcard pattern to include files and directories. This parameter is only\n"
                    + "needed for some project types, eg XLIFF, Properties. Usage\n"
                    + "--includes=\"src/myfile*.xml,**/*.xlf\"")
    public
            void setIncludes(String includes) {
        if (includes != null) {
            this.includes =
                    ImmutableList.copyOf(splitter.split(includes));
        } else {
            this.includes = ImmutableList.of();
        }
    }

    @Override
    public ImmutableList<String> getExcludes() {
        return excludes;
    }

    @Option(
            name = "--excludes",
            metaVar = "EXCLUDES",
            usage = "Wildcard pattern to exclude files and directories. Usage\n"
                    + "--excludes=\"Pattern1,Pattern2,Pattern3\"")
    public
            void setExcludes(String excludes) {
        if (excludes != null) {
            this.excludes = ImmutableList.copyOf(splitter.split(excludes));
        } else {
            this.excludes = ImmutableList.of();
        }
    }
}
