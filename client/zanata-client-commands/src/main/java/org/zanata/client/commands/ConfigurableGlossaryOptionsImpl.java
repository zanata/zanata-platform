package org.zanata.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class ConfigurableGlossaryOptionsImpl extends ConfigurableOptionsImpl
    implements ConfigurableGlossaryOptions {

    /**
     * Configuration file for Zanata client.
     */
    private File config = new File("zanata.xml");

    private String project;

    @Override
    public File getConfig() {
        return config;
    }

    @Override
    public String getProject() {
        return project;
    }

    @Option(name = "--config", metaVar = "FILENAME",
        usage = "Configuration file, eg zanata.xml",
        required = false)
    public void setConfig(File config) {
        this.config = config;
    }

    @Option(name = "--project", metaVar = "PROJ",
        usage = "Required for project glossary")
    public void setProject(String project) {
        this.project = project;
    }
}
