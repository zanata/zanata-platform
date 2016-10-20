package org.zanata.client.commands.glossary.delete;

import org.zanata.client.commands.ConfigurableGlossaryOptions;

public interface GlossaryDeleteOptions extends ConfigurableGlossaryOptions {
    public String getId();

    public boolean getAllGlossary();
}
