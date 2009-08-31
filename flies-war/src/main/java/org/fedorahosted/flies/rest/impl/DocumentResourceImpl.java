package org.fedorahosted.flies.rest.impl;

import javax.ws.rs.Path;

import org.fedorahosted.flies.rest.DocumentResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.Resource;
import org.jboss.seam.annotations.Name;

import net.openl10n.packaging.jpa.project.HProject;

@Path("/documents")
@Name("documentResource")
public class DocumentResourceImpl implements DocumentResource{

	private HProject project;
	
	public void setProject(HProject project) {
		this.project = project;
	}
	
	@Override
	public Document get(String includeTargets) {
		return null;
	}

	@Override
	public Resource getResource(String resId) {
		return null;
	}

	// hack to allow sub-resource in resteasy
	public static DocumentResource getProxyWrapper(final DocumentResource instance){
		return new DocumentResource(){

			@Override
			public Document get(String includeTargets) {
				return instance.get(includeTargets);
			}

			@Override
			public Resource getResource(String resId) {
				return instance.getResource(resId);
			}

		};
	}		

}
