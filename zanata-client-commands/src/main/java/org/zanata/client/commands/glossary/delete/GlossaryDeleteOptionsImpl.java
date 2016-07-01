package org.zanata.client.commands.glossary.delete;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableGlossaryOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

public class GlossaryDeleteOptionsImpl extends ConfigurableGlossaryOptionsImpl
        implements GlossaryDeleteOptions {

    private String id;
    private boolean allGlossary = false;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean getAllGlossary() {
        return allGlossary;
    }

    @Option(name = "--id", metaVar = "ID",
        usage = "id of a glossary entry to delete.")
    public void setId(String id) {
        this.id = id;
    }

    @Option(name = "--all", metaVar = "ALL",
        usage = "Delete entire glossaries from the server. Default: false")
    public void setAllGlossary(boolean allGlossary) {
        this.allGlossary = allGlossary;
    }

    @Override
    public ZanataCommand initCommand() {
        return new GlossaryDeleteCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-delete";
    }

    @Override
    public String getCommandDescription() {
        return "Delete glossary entries in Zanata";
    }
}
