package org.zanata.client.commands.glossary.push;

import java.io.File;

import org.zanata.client.commands.ConfigurableGlossaryOptions;
import org.zanata.client.commands.ConfigurableOptions;

public interface GlossaryPushOptions extends ConfigurableGlossaryOptions {
    public File getGlossaryFile();

    public String getSourceLang();

    public String getTransLang();

    public int getBatchSize();
}
