package org.zanata.client.commands;

public interface ZanataCommand {
    /**
     * Executes before-actions, the command, then after-actions. Before- and
     * after-actions are specified in command hooks, and will be skipped if they
     * are not supported for the command or if none are specified. The command
     * is executed using the parameters which have been previously set. This
     * method must be called after initConfig().
     *
     * @see {@link org.zanata.client.config.CommandHook}
     */
    public void runWithActions() throws Exception;

    /**
     * Returns true if the command has been deprecated.
     *
     * @return
     */
    boolean isDeprecated();

    /**
     * If the command has been deprecated, returns a message (eg a command which
     * replaces the deprecated command).
     *
     * @return
     */
    String getDeprecationMessage();

    /**
     * Returns the command name (eg Maven goal name)
     *
     * @return
     */
    String getName();
}
