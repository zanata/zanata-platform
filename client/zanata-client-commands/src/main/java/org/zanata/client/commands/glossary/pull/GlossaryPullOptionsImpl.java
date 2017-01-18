package org.zanata.client.commands.glossary.pull;


import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableGlossaryOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

import com.google.common.collect.ImmutableList;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryPullOptionsImpl extends ConfigurableGlossaryOptionsImpl
        implements GlossaryPullOptions {

    private String fileType;
    private String[] transLang;

    @Option(name = "--file-type", metaVar = "(CSV or PO)",
        usage = "File type to be downloaded.\n" +
            "csv (default) - csv file format with comma separated values\n" +
            "po - a zip file of po files on available locales")
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Option(name = "--trans-lang", metaVar = "LOCALE1,LOCALE2",
            usage = "Translation languages to pull from Zanata.\nLeave empty for all available languages.")
    public void setTransLang(String transLang) {
        this.transLang = transLang.split(",");
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public ImmutableList<String> getTransLang() {
        if (transLang != null) {
            return ImmutableList.copyOf(transLang);
        }
        return ImmutableList.of();
    }

    @Override
    public ZanataCommand initCommand() {
        return new GlossaryPullCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-pull";
    }

    @Override
    public String getCommandDescription() {
        return "Pull glossary file from Zanata";
    }
}
