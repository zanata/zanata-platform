package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ListRemoteCommand;

/**
 * Lists all remote documents in the configured Zanata project version.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Mojo(name = "list-remote", requiresOnline = true, requiresProject = false)
public class ListRemoteMojo extends
        ConfigurableProjectMojo<ConfigurableProjectOptions> {

    private static final String ZANATA_SERVER_PROJECT_TYPE = "remote";

    public ListRemoteMojo() throws Exception {
        super();
    }

    public ListRemoteCommand initCommand() {
        return new ListRemoteCommand(this);
    }

    @Override
    public String getProjectType() {
        return ZANATA_SERVER_PROJECT_TYPE;
    }

    @Override
    public String getCommandName() {
        return "list-remote";
    }

    @Override
    public boolean isAuthRequired() {
        return false;
    }
}
