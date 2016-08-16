package org.zanata.client.commands.glossary.push;

import org.zanata.client.commands.ConfigurableGlossaryOptions;

import java.nio.file.Path;

public interface GlossaryPushOptions extends ConfigurableGlossaryOptions {
    public String DEFAULT_SOURCE_LANG = "en-US";
    public int DEFAULT_BATCH_SIZE = 50;

    public Path getFile();

    public String getTransLang();

    public int getBatchSize();
}
