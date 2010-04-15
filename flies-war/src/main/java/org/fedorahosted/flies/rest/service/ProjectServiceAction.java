package org.fedorahosted.flies.rest.service;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.dto.Project;

public interface ProjectServiceAction {
	
	public Response get(String projectSlug);
	public Response put(Project project);

}
