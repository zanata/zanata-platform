package org.zanata.client.commands.glossary.delete;

import org.zanata.client.commands.ConfigurableProjectOptions;

public interface GlossaryDeleteOptions extends ConfigurableProjectOptions {
    public String getId();

    public boolean getAllGlossary();
}
