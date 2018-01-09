package org.zanata.client.commands;

import java.util.List;

import javax.annotation.Nonnull;

import org.zanata.client.config.CommandHook;

public interface BasicOptions {
    ZanataCommand initCommand();

    /**
     * Whether to enable debug mode. Defaults to the value in zanata.ini. This
     * value is used by command line clients, but not by Maven (which uses its
     * own --debug/-X flag).
     */
    boolean getDebug();

    void setDebug(boolean debug);

    boolean isDebugSet();

    /**
     * Whether to display full information about errors (ie exception stack
     * traces). Defaults to the value in zanata.ini. This value is used by
     * command line clients, but not by Maven (which uses its own --errors/-e
     * flag).
     */
    boolean getErrors();

    void setErrors(boolean errors);

    boolean isErrorsSet();

    /**
     * Whether to display the command's usage help. Maven uses the
     * auto-generated HelpMojo instead.
     */
    boolean getHelp();

    void setHelp(boolean help);

    /**
     * Enable quiet mode - error messages only
     */
    boolean getQuiet();

    void setQuiet(boolean quiet);

    boolean isQuietSet();

    boolean isInteractiveMode();

    void setInteractiveMode(boolean interactiveMode);

    boolean isInteractiveModeSet();

    /**
     * Used to generate the command line interface and its usage help. This name
     * should match the Maven Mojo's 'goal' annotation and must match the @SubCommand
     * name in ZanataClient.
     *
     * @return
     */
    String getCommandName();

    /**
     * Used to generate CLI usage help. This description should preferably match
     * the Maven Mojo's Javadoc description.
     *
     * @return
     */
    String getCommandDescription();

    @Nonnull List<CommandHook> getCommandHooks();

    void setCommandHooks(@Nonnull List<CommandHook> commandHooks);
}
