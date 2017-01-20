package org.zanata.client.commands;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionOptionsImpl extends ConfigurableOptionsImpl implements
        PutVersionOptions {
    private String versionProject;
    private String versionSlug;
    private String projectType;
    private String versionStatus;

    @Override
    public String getCommandName() {
        return "put-version";
    }

    @Override
    public String getCommandDescription() {
        return "Creates or updates a Zanata project version.";
    }

    @Override
    public PutVersionCommand initCommand() {
        return new PutVersionCommand(this);
    }

    @Override
    @Option(name = "--version-project", metaVar = "PROJ",
            usage = "ID of Zanata project", required = true)
    public void setVersionProject(String id) {
        this.versionProject = id;
    }

    @Override
    @Option(name = "--version-slug", metaVar = "VER",
            usage = "Project version ID", required = true)
    public void setVersionSlug(String id) {
        this.versionSlug = id;
    }

    @Override
    @Option(name = "--project-type", metaVar = "TYPE", usage = "Project Type")
    public void setProjectType(String type) {
        this.projectType = type;
    }

    @Option(name = "--version-status",
            metaVar = "STATUS",
            usage = "Version status (active, readonly)")
    public void setVersionStatus(String status) {
        this.versionStatus = status;
    }

    @Override
    public String getVersionProject() {
        return versionProject;
    }

    @Override
    public String getVersionSlug() {
        return versionSlug;
    }

    @Override
    public String getProjectType() {
        return projectType;
    }

    @Override
    public String getVersionStatus() {
        return versionStatus;
    }

}
