package org.zanata.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.ConfigurableGlossaryOptions;
import org.zanata.client.commands.ConfigurableOptions;

/**
 * Base mojo for glossary commands.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class GlossaryMojo<O extends ConfigurableOptions>
        extends ConfigurableMojo<O> implements ConfigurableGlossaryOptions {
    /**
     * Zanata configuration file.
     */
    @Parameter(property = "zanata.config", defaultValue = "${basedir}/zanata.xml")
    private File config;

    /**
     * Project slug (id) within Zanata server. Required for project glossary.
     */
    @Parameter(property = "zanata.project")
    private String project;

    @Override
    public File getConfig() {
        return config;
    }

    @Override
    public void setConfig(File config) {
        this.config = config;
    }

    @Override
    public String getProject() {
        return project;
    }

    @Override
    public void setProject(String project) {
        this.project = project;
    }
}
