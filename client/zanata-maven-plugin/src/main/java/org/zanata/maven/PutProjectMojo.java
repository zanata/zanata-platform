package org.zanata.maven;

import org.zanata.client.commands.PutProjectCommand;
import org.zanata.client.commands.PutProjectOptions;

/**
 * Creates or updates a Zanata project.
 *
 * @goal put-project
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public class PutProjectMojo extends ConfigurableMojo<PutProjectOptions>
        implements PutProjectOptions {

    /**
     * Project slug/ID
     *
     * @parameter expression="${zanata.projectSlug}"
     * @required
     */
    private String projectSlug;

    /**
     * Project name
     *
     * @parameter expression="${zanata.projectName}"
     * @required
     */
    private String projectName;

    /**
     * Project description
     *
     * @parameter expression="${zanata.projectDesc}"
     * @required
     */
    private String projectDesc;

    /**
     * URL for original source in a human-readable format, e.g.
     * https://github.com/zanata/zanata
     *
     * @parameter expression="${zanata.sourceViewUrl}"
     */
    private String sourceViewUrl;

    /**
     * URL for original source in a machine-readable format, e.g.
     * git@github.com:zanata/zanata.git
     *
     * @parameter expression="${zanata.sourceCheckoutUrl}"
     */
    private String sourceCheckoutUrl;

    /**
     * Default Project type. Versions under this project that do not specify a
     * project type will use this default. Valid values are {utf8properties,
     * properties, gettext, podir, xliff, xml, file}.
     *
     * See https://github.com/zanata/zanata/wiki/Project-Types
     *
     * @parameter expression="${zanata.defaultProjectType}"
     * @required
     */
    private String defaultProjectType;

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
