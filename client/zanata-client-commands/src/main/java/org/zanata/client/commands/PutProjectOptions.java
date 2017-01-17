package org.zanata.client.commands;

import org.kohsuke.args4j.Option;

public interface PutProjectOptions extends ConfigurableOptions {

    @Option(name = "--project-slug", metaVar = "PROJ", usage = "Project ID",
            required = true)
    public void setProjectSlug(String id);

    @Option(name = "--project-name", metaVar = "NAME", required = true,
            usage = "Project name")
    public void setProjectName(String name);

    @Option(name = "--project-desc", metaVar = "DESC", required = true,
            usage = "Project description")
    public void setProjectDesc(String desc);

    @Option(name = "--source-view-url",
            metaVar = "SRCVURL",
            required = false,
            usage = "URL for original source in a human-readable format, e.g. https://github.com/zanata/zanata")
    public void setSourceViewUrl(String sourceViewUrl);

    @Option(name = "--source-checkout-url",
            metaVar = "SRCURL",
            required = false,
            usage = "URL for original source in a machine-readable format, e.g. git@github.com:zanata/zanata.git")
    public void setSourceCheckoutUrl(String sourceCheckoutUrl);

    @Option(name = "--default-project-type",
            metaVar = "TYPE",
            required = true,
            usage = "Default project type. Versions under this project that do not specify a project type will use this default. Valid values are : Utf8Properties, Properties, Gettext, Podir, Xliff, Xml, File. See https://github.com/zanata/zanata/wiki/Project-Types")
    public void setDefaultProjectType(String type);

    @Option(name = "--project-status",
            metaVar = "STATUS",
            required = true,
            usage = "Project status (active, readonly)")
    public void setProjectStatus(String status);

    public String getProjectSlug();

    public String getProjectDesc();

    public String getProjectName();

    public String getSourceViewUrl();

    public String getSourceCheckoutUrl();

    public String getDefaultProjectType();

    public String getProjectStatus();

}
