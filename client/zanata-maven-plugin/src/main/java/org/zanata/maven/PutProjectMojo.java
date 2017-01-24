package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.PutProjectCommand;
import org.zanata.client.commands.PutProjectOptions;

/**
 * Creates or updates a Zanata project.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Mojo(name = "put-project", requiresOnline = true, requiresProject = false)
public class PutProjectMojo extends ConfigurableMojo<PutProjectOptions>
        implements PutProjectOptions {

    /**
     * Project slug/ID
     */
    @Parameter(property = "zanata.projectSlug", required = true)
    private String projectSlug;

    /**
     * Project name
     */
    @Parameter(property = "zanata.projectName")
    private String projectName;

    /**
     * Project description
     */
    @Parameter(property = "zanata.projectDesc")
    private String projectDesc;

    /**
     * URL for original source in a human-readable format, e.g.
     * https://github.com/zanata/zanata
     */
    @Parameter(property = "zanata.sourceViewUrl")
    private String sourceViewUrl;

    /**
     * URL for original source in a machine-readable format, e.g.
     * git@github.com:zanata/zanata.git
     */
    @Parameter(property = "zanata.sourceCheckoutUrl")
    private String sourceCheckoutUrl;

    /**
     * Default Project type. Versions under this project that do not specify a
     * project type will use this default. Valid values are {utf8properties,
     * properties, gettext, podir, xliff, xml, file}.
     *
     * See https://github.com/zanata/zanata/wiki/Project-Types
     */
    @Parameter(property = "zanata.defaultProjectType")
    private String defaultProjectType;

    /**
     * Project status (active, readonly)
     */
    @Parameter(property = "zanata.projectStatus")
    private String projectStatus;

    public PutProjectMojo() throws Exception {
        super();
    }

    public PutProjectCommand initCommand() {
        return new PutProjectCommand(this);
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getProjectStatus() {
        return this.projectStatus;
    }

    @Override
    public String getSourceViewUrl() {
        return sourceViewUrl;
    }

    @Override
    public void setSourceViewUrl(String sourceViewUrl) {
        this.sourceViewUrl = sourceViewUrl;
    }

    @Override
    public String getSourceCheckoutUrl() {
        return sourceCheckoutUrl;
    }

    @Override
    public void setSourceCheckoutUrl(String sourceCheckoutUrl) {
        this.sourceCheckoutUrl = sourceCheckoutUrl;
    }

    public String getDefaultProjectType() {
        return defaultProjectType;
    }

    public void setDefaultProjectType(String defaultProjectType) {
        this.defaultProjectType = defaultProjectType;
    }

    @Override
    public String getCommandName() {
        return "put-project";
    }
}
