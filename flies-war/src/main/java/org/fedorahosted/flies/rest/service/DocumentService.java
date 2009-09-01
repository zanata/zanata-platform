package org.fedorahosted.flies.rest.service;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jboss.seam.annotations.Name;

@Name("documentService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}/documents")
public class DocumentService {
	
	@PathParam("projectSlug")
	private String projectSlug;
	
	@PathParam("iterationSlug")
	private String iterationSlug;
	
}
