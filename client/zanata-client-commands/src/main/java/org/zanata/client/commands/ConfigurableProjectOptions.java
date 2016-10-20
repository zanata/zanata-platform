package org.zanata.client.commands;

import java.io.File;
import java.util.List;


import org.kohsuke.args4j.Option;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import com.google.common.collect.ImmutableList;

/**
 * Base options for commands which support configuration by the user's
 * zanata.ini and by a project's zanata.xml
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public interface ConfigurableProjectOptions extends ConfigurableOptions {

    public String getProj();

    @Option(
            name = "--project",
            metaVar = "PROJ",
            usage = "Project ID.  This value is required unless specified in zanata.xml.")
    public
            void setProj(String projectSlug);

    @Option(name = "--project-config", metaVar = "FILENAME",
            usage = "Project configuration file, eg zanata.xml",
            required = false)
    public void setProjectConfig(File projectConfig);

    public String getProjectVersion();

    @Option(
            name = "--project-version",
            metaVar = "VER",
            usage = "Project version ID  This value is required unless specified in zanata.xml.")
    public
            void setProjectVersion(String versionSlug);

    public String getProjectType();

    @Option(
            aliases = { "-T" },
            name = "--project-type",
            metaVar = "PROJTYPE",
            usage = "Project type  This value is required unless specified in zanata.xml.")
    public
            void setProjectType(String projectType);

    public File getProjectConfig();

    public LocaleList getLocaleMapList();

    public void setLocaleMapList(LocaleList locales);

    @Option(
            aliases = { "-s" },
            name = "--src-dir",
            metaVar = "DIR",
            required = true,
            usage = "Base directory for source files (eg \".\", \"pot\", \"src/main/resources\")")
    void setSrcDir(File srcDir);

    File getSrcDir();

    @Option(
            aliases = { "-t" },
            name = "--trans-dir",
            metaVar = "DIR",
            required = true,
            usage = "Base directory for translated files (eg \".\", \"po\", \"src/main/resources\")")
    void setTransDir(File transDir);

    File getTransDir();

    ImmutableList<String> getIncludes();

    ImmutableList<String> getExcludes();

    @Option(
            name = "--includes",
            metaVar = "INCLUDES",
            usage = "Wildcard pattern to include files and directories. This parameter is only\n"
                    + "needed for some project types, eg XLIFF, Properties. Usage\n"
                    + "--includes=\"src/myfile*.xml,**/*.xlf\"")
    void setIncludes(String includes);

    @Option(
            name = "--excludes",
            metaVar = "EXCLUDES",
            usage = "Wildcard pattern to exclude files and directories. Usage\n"
                    + "--excludes=\"Pattern1,Pattern2,Pattern3\"")
    void setExcludes(String excludes);

    void setFileMappingRules(List<FileMappingRule> rules);

    List<FileMappingRule> getFileMappingRules();
}
