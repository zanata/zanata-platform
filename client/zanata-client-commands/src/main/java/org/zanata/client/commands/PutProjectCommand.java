package org.zanata.client.commands;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.dto.Project;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
        String projectId = getOpts().getProjectSlug();
        Project project = getClientFactory().getProjectClient(projectId).get();
        String op;
        if (project == null) {
            op = "Create new project {}";
            project = new Project();
            project.setId(getOpts().getProjectSlug());
            if (isBlank(getOpts().getProjectName()) ||
                    isBlank(getOpts().getDefaultProjectType())) {
                throw new Exception(
                    "default-project-type and project-name must be specified for new projects");
            }
        } else {
            op = "Update project {}";
        }
        log.info(op, projectId);

        project.setStatus(firstNonNull(statusFromString(getOpts().getProjectStatus()),
                project.getStatus()));
        project.setName(firstNonNull(getOpts().getProjectName(),
                project.getName()));
        project.setDescription(firstNonNull(getOpts().getProjectDesc(),
                project.getDescription()));
        project.setSourceViewURL(firstNonNull(getOpts().getSourceViewUrl(),
                project.getSourceViewURL()));
        project.setSourceCheckoutURL(firstNonNull(getOpts().getSourceCheckoutUrl(),
                project.getSourceCheckoutURL()));

        String defaultProjectType = firstNonNull(getOpts().getDefaultProjectType(),
                project.getDefaultType());
        if (isBlank(defaultProjectType)) {
            throw new Exception(
                    "Default project type must not be null or empty.");
        }
        project.setDefaultType(defaultProjectType);

        log.debug("{}", project);

        // send project to rest api
        ProjectClient client = getClientFactory().getProjectClient(
                getOpts().getProjectSlug());
        client.put(project);
        log.info(op, projectId + " complete");
    }

    private EntityStatus statusFromString(String status) throws Exception {
        if (status == null) {
            return null;
        } else if (status.equalsIgnoreCase("active")) {
            return EntityStatus.ACTIVE;
        } else if (status.equalsIgnoreCase("readonly")) {
            return EntityStatus.READONLY;
        }
        throw new Exception("Invalid value for project-status: " + status);
    }
}
