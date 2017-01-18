package org.zanata.client.commands.glossary.search;

import org.zanata.client.commands.ConfigurableGlossaryOptions;

public interface GlossarySearchOptions extends ConfigurableGlossaryOptions {

    public String getFilter();
    public String getProject();
    public boolean getRaw();
}
