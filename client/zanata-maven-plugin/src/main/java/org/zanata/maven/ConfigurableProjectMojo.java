package org.zanata.maven;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Base class for mojos which support configuration by the user's zanata.ini and
 * by a project's zanata.xml
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public abstract class ConfigurableProjectMojo<O extends ConfigurableOptions>
        extends ConfigurableMojo<O> implements ConfigurableProjectOptions {

    // @formatter:off
   /*
    * @parameter should work on setter methods - see
    * http://www.sonatype.com/books/mvnref-book/reference/writing-plugins-sect-param-annot.html
    * - but it doesn't.  So we have to put @parameter on the fields instead.
    */
   // @formatter:on

    /**
     * Zanata project configuration file.
     */
    @Parameter(property = "zanata.projectConfig", defaultValue = "${basedir}/zanata.xml")
    private File projectConfig;

    /**
     * Project slug (id) within Zanata server.
     */
    @Parameter(property = "zanata.project")
    private String project;

    /**
     * Project version slug (id) within Zanata server.
     */
    @Parameter(property = "zanata.projectVersion")
    private String projectVersion;

    /**
     * Type of project ( "properties" = Java .properties, "podir" =
     * publican-style gettext directories, "utf8properties" = UTF-8 .properties
     * files, "gettext" = gettext PO files, "file" = EXPERIMENTAL document files
     * of various types).
     *
     * If 'file' is used, transDir must not be the same as or nested within
     * srcDir, and vice versa.
     */
    @Parameter(property = "zanata.projectType")
    private String projectType;

    private LocaleList localeMapList;

    /**
     * Base directory for source-language files
     */
    @Parameter(property = "zanata.srcDir")
    private File srcDir;
    /**
     * Base directory for target-language files (translations)
     */
    @Parameter(property = "zanata.transDir")
    private File transDir;
    /**
     * Wildcard pattern to include files and directories. This parameter is only
     * needed for some project types, eg XLIFF, Properties. Usage
     * -Dzanata.includes="src/myfile*.xml,**&#47*.xliff.xml"
     */
    @Parameter(property = "zanata.includes")
    private String includes;
    /**
     * Wildcard pattern to exclude files and directories. Usage
     * -Dzanata.excludes="Pattern1,Pattern2,Pattern3"
     */
    @Parameter(property = "zanata.excludes")
    private String excludes;
    private Splitter splitter = Splitter.on(",").omitEmptyStrings()
            .trimResults();
    private List<FileMappingRule> rules;

    public ConfigurableProjectMojo() {
        super();
    }

    @Override
    public File getProjectConfig() {
        return projectConfig;
    }

    @Override
    public void setProjectConfig(File projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    public String getProj() {
        return project;
    }

    public String getProject() {
        return project;
    }

    @Override
    public void setProj(String project) {
        this.project = project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @Override
    public String getProjectVersion() {
        return projectVersion;
    }

    @Override
    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    @Override
    public String getProjectType() {
        return projectType;
    }

    @Override
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    @Override
    public File getSrcDir() {
        return srcDir;
    }

    @Override
    public File getTransDir() {
        return transDir;
    }

    @Override
    public LocaleList getLocaleMapList() {
        return localeMapList;
    }

    @Override
    public void setLocaleMapList(LocaleList localeMapList) {
        this.localeMapList = localeMapList;
    }

    @Override
    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    @Override
    public void setTransDir(File transDir) {
        this.transDir = transDir;
    }

    @Override
    public ImmutableList<String> getIncludes() {
        if (includes != null) {
            return ImmutableList.copyOf(splitter.split(includes));
        }
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<String> getExcludes() {
        if (excludes != null) {
            return ImmutableList.copyOf(splitter.split(excludes));
        }
        return ImmutableList.of();
    }

    @Override
    public void setIncludes(String includes) {
        this.includes = includes;
    }

    @Override
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @Override
    public void setFileMappingRules(List<FileMappingRule> rules) {
        this.rules = rules;
    }

    @Override
    public List<FileMappingRule> getFileMappingRules() {
        if (rules == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(rules);
    }
}
