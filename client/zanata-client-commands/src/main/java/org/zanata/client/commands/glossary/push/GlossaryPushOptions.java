package org.zanata.client.commands.glossary.push;

import java.io.File;

import org.zanata.client.commands.ConfigurableGlossaryOptions;

public interface GlossaryPushOptions extends ConfigurableGlossaryOptions {
    public String DEFAULT_SOURCE_LANG = "en-US";
    public int DEFAULT_BATCH_SIZE = 50;

    public File getFile();

    public String getTransLang();

    public int getBatchSize();
}
