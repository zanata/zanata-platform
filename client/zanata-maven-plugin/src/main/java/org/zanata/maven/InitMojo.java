package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.init.InitCommand;
import org.zanata.client.commands.init.InitOptions;

/**
 * Initialize Zanata project configuration.
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Mojo(name = "init", requiresOnline = true, requiresProject = false)
public class InitMojo extends ConfigurableProjectMojo<InitOptions> implements InitOptions {
    @Override
    public ConfigurableCommand<InitOptions> initCommand() {
        return new InitCommand(this);
    }

    @Override
    public String getCommandName() {
        return "init";
    }
}
