package org.zanata.client.commands;

public interface ZanataCommand {
    /**
     * Executes the command, using the parameters which have been previously
     * set. This method must be called after initConfig().
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
