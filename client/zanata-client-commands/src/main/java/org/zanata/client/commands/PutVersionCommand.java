package org.zanata.client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.dto.ProjectIteration;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionCommand extends ConfigurableCommand<PutVersionOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PutVersionCommand.class);

    public PutVersionCommand(PutVersionOptions opts) {
        super(opts);
    }

    @Override
    public void run() throws Exception {
        ProjectIteration version = new ProjectIteration();
        version.setId(getOpts().getVersionSlug());

        if (getOpts().getProjectType() != null) {
            if (getOpts().getProjectType().isEmpty()) {
                throw new Exception(
                        "If a project type is specified, it must not be empty.");
            }
            version.setProjectType(getOpts().getProjectType());
        }
        log.debug("{}", version);

        ProjectIterationClient client = getClientFactory()
                .getProjectIterationClient(
                        getOpts().getVersionProject(),
                        getOpts().getVersionSlug());

        client.put(version);
    }

}
