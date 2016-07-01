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

    @Override
    public File getConfig() {
        return config;
    }

    @Option(name = "--config", metaVar = "FILENAME",
        usage = "Configuration file, eg zanata.xml",
        required = false)
    public void setConfig(File config) {
        this.config = config;
    }

}
