package org.zanata.client.commands;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ConfigurableGlossaryOptions extends ConfigurableOptions {

    public File getConfig();

    @Option(name = "--config", metaVar = "FILENAME",
        usage = "Configuration file, eg zanata.xml",
        required = false)
    public void setConfig(File config);
}
