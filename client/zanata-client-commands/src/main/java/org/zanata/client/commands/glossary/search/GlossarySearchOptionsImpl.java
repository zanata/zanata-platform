package org.zanata.client.commands.glossary.search;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableGlossaryOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

public class GlossarySearchOptionsImpl extends ConfigurableGlossaryOptionsImpl
        implements GlossarySearchOptions {

    private String filter;
    private String project;
    private boolean raw;

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public String getProject() {
        return this.project;
    }

    @Override
    public boolean getRaw() {
        return this.raw;
    }

    @Option(name = "--filter", metaVar = "FILTER", required = true,
            usage = "Search term for glossary entries.")
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Option(name = "--project-glossary", metaVar = "PROJECT",
            usage = "Project Glossary ID to restrict search.")
    public void setProject(String project) {
        this.project = project;
    }

    @Option(name = "--raw", metaVar = "RAW",
            usage = "Print the raw JSON response.")
    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    @Override
    public ZanataCommand initCommand() {
        return new GlossarySearchCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-search";
    }

    @Override
    public String getCommandDescription() {
        return "Find glossary entries in Zanata";
    }
}
