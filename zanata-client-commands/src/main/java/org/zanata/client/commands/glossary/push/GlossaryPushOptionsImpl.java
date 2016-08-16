package org.zanata.client.commands.glossary.push;

import java.io.File;
import java.nio.file.Path;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableGlossaryOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryPushOptionsImpl extends ConfigurableGlossaryOptionsImpl
        implements GlossaryPushOptions {

    private Path file;
    private String transLang;
    private int batchSize = DEFAULT_BATCH_SIZE;

    @Option(name = "--file",
            usage = "Location path for the glossary file.", required = true)
    public void setFile(Path file) {
        this.file = file;
    }

    @Option(name = "--trans-lang", metaVar = "LOCALE",
            usage = "Translation language of the file.\n"
                    + "Not required for csv file type")
    public void setTransLang(String transLang) {
        this.transLang = transLang;
    }

    @Option(name = "--batch-size", metaVar = "50",
            usage = "Batch size to upload for large glossary file. (defaults to 50)")
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public Path getFile() {
        return file;
    }

    @Override
    public String getTransLang() {
        return transLang;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public ZanataCommand initCommand() {
        return new GlossaryPushCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-push";
    }

    @Override
    public String getCommandDescription() {
        return "Push glossary to Zanata";
    }
}
