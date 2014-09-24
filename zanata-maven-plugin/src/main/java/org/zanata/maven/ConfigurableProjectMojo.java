package org.zanata.maven;

import java.io.File;

import org.zanata.client.commands.ConfigurableOptions;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.config.LocaleList;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Base class for mojos which support configuration by the user's zanata.ini and
 * by a project's zanata.xml
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
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
     *
     * @parameter expression="${zanata.projectConfig}"
     *            default-value="${basedir}/zanata.xml"
     */
    private File projectConfig;

    /**
     * Project slug (id) within Zanata server.
     *
     * @parameter expression="${zanata.project}"
     */
    private String project;

    /**
     * Project version slug (id) within Zanata server.
     *
     * @parameter expression="${zanata.projectVersion}"
     */
    private String projectVersion;

    /**
     * Type of project ( "properties" = Java .properties, "podir" =
     * publican-style gettext directories, "utf8properties" = UTF-8 .properties
     * files, "gettext" = gettext PO files, "file" = EXPERIMENTAL document files
     * of various types).
     *
     * If 'file' is used, transDir must not be the same as or nested within
     * srcDir, and vice versa.
     *
     * @parameter expression="${zanata.projectType}"
     */
    private String projectType;

    private LocaleList localeMapList;

    /**
     * Base directory for source-language files
     *
     * @parameter expression="${zanata.srcDir}"
     */
    private File srcDir;
    /**
     * Base directory for target-language files (translations)
     *
     * @parameter expression="${zanata.transDir}"
     */
    private File transDir;
    /**
     * Wildcard pattern to include files and directories. This parameter is only
     * needed for some project types, eg XLIFF, Properties. Usage
     * -Dzanata.includes="src/myfile*.xml,**&#47*.xliff.xml"
     *
     * @parameter expression="${zanata.includes}"
     */
    private String includes;
    /**
     * Wildcard pattern to exclude files and directories. Usage
     * -Dzanata.excludes="Pattern1,Pattern2,Pattern3"
     *
     * @parameter expression="${zanata.excludes}"
     */
    private String excludes;
    private Splitter splitter = Splitter.on(",").omitEmptyStrings()
            .trimResults();

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

    @Override
    public void setProj(String project) {
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
}
