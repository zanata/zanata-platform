package org.zanata.maven;

import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ListLocalCommand;

/* @Mojo commented out so that maven won't include this unfinished mojo in the plugin:
 * [NOT YET IMPLEMENTED] Lists all local files in the project which are
 * considered to be Zanata documents. These are the files which will be sent to
 * Zanata when using the 'push' goal.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
//@Mojo(name = "list-local", requiresProject = false)
public class ListLocalMojo extends
        ConfigurableProjectMojo<ConfigurableProjectOptions> {

    public ListLocalMojo() throws Exception {
        super();
    }

    public ListLocalCommand initCommand() {
        return new ListLocalCommand(this);
    }

    @Override
    public String getCommandName() {
        return "list-local";
    }
}
