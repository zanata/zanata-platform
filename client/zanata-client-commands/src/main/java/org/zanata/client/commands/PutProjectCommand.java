package org.zanata.client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.dto.Project;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutProjectCommand extends ConfigurableCommand<PutProjectOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PutProjectCommand.class);

    public PutProjectCommand(PutProjectOptions opts) {
        super(opts);
    }

    @Override
    public void run() throws Exception {
        Project project = new Project();
        project.setId(getOpts().getProjectSlug());
        project.setName(getOpts().getProjectName());
        project.setDescription(getOpts().getProjectDesc());
        project.setSourceViewURL(getOpts().getSourceViewUrl());
        project.setSourceCheckoutURL(getOpts().getSourceCheckoutUrl());

        if (getOpts().getDefaultProjectType() == null
                || getOpts().getDefaultProjectType().isEmpty()) {
            throw new Exception(
                    "Default project type must not be null or empty.");
        }
        project.setDefaultType(getOpts().getDefaultProjectType());

        log.debug("{}", project);

        // send project to rest api
        ProjectClient client = getClientFactory().getProjectClient(
                getOpts().getProjectSlug());
        client.put(project);
    }
}
