package org.zanata.client.commands.glossary.pull;

import org.zanata.client.commands.ConfigurableGlossaryOptions;
import org.zanata.client.commands.ConfigurableOptions;

import com.google.common.collect.ImmutableList;

public interface GlossaryPullOptions extends ConfigurableGlossaryOptions {
    public String getFileType();

    public ImmutableList<String> getTransLang();
}
