package org.zanata.client.commands;

import org.kohsuke.args4j.Option;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectOptionsImpl extends ConfigurableOptionsImpl implements
        PutProjectOptions {

    private String projectSlug;
    private String projectName;
    private String projectDesc;
    private String sourceViewUrl;
    private String sourceCheckoutUrl;
    private String defaultProjectType;

    @Override
    public String getCommandName() {
        return "put-project";
    }

    @Override
    public String getCommandDescription() {
        return "Creates or updates a Zanata project.";
    }

    @Override
    public PutProjectCommand initCommand() {
        return new PutProjectCommand(this);
    }

    @Override
    @Option(name = "--project-slug", metaVar = "PROJ", usage = "Project ID",
            required = true)
    public void setProjectSlug(String id) {
        this.projectSlug = id;
    }

    @Override
    @Option(name = "--project-name", metaVar = "NAME", required = true,
            usage = "Project name")
    public void setProjectName(String name) {
        this.projectName = name;
    }

    @Override
    @Option(name = "--project-desc", metaVar = "DESC", required = true,
            usage = "Project description")
    public void setProjectDesc(String desc) {
        this.projectDesc = desc;
    }

    @Override
    @Option(
            name = "--source-view-url",
            metaVar = "SRCVURL",
            required = false,
            usage = "URL for original source in a human-readable format, e.g. https://github.com/zanata/zanata")
    public
            void setSourceViewUrl(String sourceViewUrl) {
        this.sourceViewUrl = sourceViewUrl;
    }

    @Override
    @Option(
            name = "--source-checkout-url",
            metaVar = "SRCURL",
            required = false,
            usage = "URL for original source in a machine-readable format, e.g. git@github.com:zanata/zanata.git")
    public
            void setSourceCheckoutUrl(String sourceCheckoutUrl) {
        this.sourceCheckoutUrl = sourceCheckoutUrl;
    }

    @Override
    @Option(
            name = "--default-project-type",
            metaVar = "TYPE",
            required = true,
            usage = "Default project type. Versions under this project that do not specify a project type will use this default. Valid values are : Utf8Properties, Properties, Gettext, Podir, Xliff, Xml, File. See https://github.com/zanata/zanata/wiki/Project-Types")
    public
            void setDefaultProjectType(String defaultProjectType) {
        this.defaultProjectType = defaultProjectType;
    }

    @Override
    public String getProjectSlug() {
        return projectSlug;
    }

    @Override
    public String getProjectDesc() {
        return projectDesc;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getSourceViewUrl() {
        return sourceViewUrl;
    }

    @Override
    public String getSourceCheckoutUrl() {
        return sourceCheckoutUrl;
    }

    @Override
    public String getDefaultProjectType() {
        return defaultProjectType;
    }

}
