package org.zanata.client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
// FIXME not implemented yet!
public class ListLocalCommand extends
        ConfigurableProjectCommand<ConfigurableProjectOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(ListLocalCommand.class);

    public ListLocalCommand(ConfigurableProjectOptions opts) {
        super(opts);
    }

    @Override
    public void run() {
        // TODO remove this
        log.debug("listlocal");

        // TODO needs DocSet support
    }

}
