package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

@MappedSuperclass
public abstract class HParentResource extends HDocumentResource{

	private static final long serialVersionUID = 5832666152954738196L;

	private List<HDocumentResource> resources = new ArrayList<HDocumentResource>();
	
	public HParentResource() {
	}
	
	public HParentResource(DocumentResource res, int nextDocRev) {
		super(res, nextDocRev);
	}

//	@OneToMany(mappedBy="parent", cascade = CascadeType.ALL)
	@OneToMany(cascade = CascadeType.ALL)
	@IndexColumn(name = "pos", base=0, nullable=true)// see http://opensource.atlassian.com/projects/hibernate/browse/HHH-4390?focusedCommentId=30964&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_30964
	@Where(clause="obsolete=0")
//	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<HDocumentResource> getResources() {
		return resources;
	}
	
	public void setResources(List<HDocumentResource> children) {
		this.resources = children;
	}
	
}
