package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.PutVersionCommand;
import org.zanata.client.commands.PutVersionOptions;

/**
 * Creates or updates a Zanata project version.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Mojo(name = "put-version", requiresOnline = true, requiresProject = false)
public class PutVersionMojo extends ConfigurableMojo<PutVersionOptions>
        implements PutVersionOptions {

    /**
     * ID of Zanata project
     */
    @Parameter(property = "zanata.versionProject", required = true)
    private String versionProject;

    /**
     * Project version ID
     */
    @Parameter(property = "zanata.versionSlug", required = true)
    private String versionSlug;

    /**
     * Project type {utf8properties, properties, gettext, podir, xliff, xml,
     * file} Leave blank to inherit default project type from parent project
     */
    @Parameter(property = "zanata.projectType")
    private String projectType;

    /**
     * Version status (active, readonly)
     */
    @Parameter(property = "zanata.versionStatus")
    private String versionStatus;

    public PutVersionMojo() throws Exception {
        super();
    }

    public PutVersionCommand initCommand() {
        return new PutVersionCommand(this);
    }

    public String getVersionProject() {
        return versionProject;
    }

    public void setVersionProject(String versionProject) {
        this.versionProject = versionProject;
    }

    public String getVersionSlug() {
        return versionSlug;
    }

    public void setVersionSlug(String versionSlug) {
        this.versionSlug = versionSlug;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getVersionStatus() {
        return this.versionStatus;
    }

    public void setVersionStatus(String status) {
        this.versionStatus = status;
    }

    @Override
    public String getCommandName() {
        return "put-version";
    }
}
