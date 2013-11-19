package org.zanata.client.commands;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.args4j.Option;
import org.zanata.client.config.CommandHook;
import org.zanata.client.config.LocaleList;

/**
 * Base options for commands which support configuration by the user's
 * zanata.ini and by a project's zanata.xml
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public interface ConfigurableProjectOptions extends ConfigurableOptions {

    public String getProj();

    @Option(
            name = "--project",
            metaVar = "PROJ",
            usage = "Project ID.  This value is required unless specified in zanata.xml.")
    public
            void setProj(String projectSlug);

    @Option(name = "--project-config", metaVar = "FILENAME",
            usage = "Project configuration file, eg zanata.xml",
            required = false)
    public void setProjectConfig(File projectConfig);

    public String getProjectVersion();

    @Option(
            name = "--project-version",
            metaVar = "VER",
            usage = "Project version ID  This value is required unless specified in zanata.xml.")
    public
            void setProjectVersion(String versionSlug);

    public String getProjectType();

    @Option(
            aliases = { "-T" },
            name = "--project-type",
            metaVar = "PROJTYPE",
            usage = "Project type  This value is required unless specified in zanata.xml.")
    public
            void setProjectType(String projectType);

    public File getProjectConfig();

    public LocaleList getLocaleMapList();

    public void setLocaleMapList(LocaleList locales);

    public @Nonnull List<CommandHook> getCommandHooks();

    public void setCommandHooks(@Nonnull List<CommandHook> commandHooks);
}
