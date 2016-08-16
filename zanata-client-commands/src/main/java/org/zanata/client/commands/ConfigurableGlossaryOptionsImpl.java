package org.zanata.client.commands;

import java.io.File;
import java.nio.file.Path;

import org.kohsuke.args4j.Option;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class ConfigurableGlossaryOptionsImpl extends ConfigurableOptionsImpl
    implements ConfigurableGlossaryOptions {

    /**
     * Configuration file for Zanata client.
     */
    private Path config = new Path("zanata.xml");

    @Override
    public Path getConfig() {
        return config;
    }

    @Option(name = "--config", metaVar = "FILENAME",
        usage = "Configuration file, eg zanata.xml",
        required = false)
    public void setConfig(Path config) {
        this.config = config;
    }

}
