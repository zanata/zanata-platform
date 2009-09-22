package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.rest.dto.Resource;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

@MappedSuperclass
public abstract class HParentResource extends HResource{

	private static final long serialVersionUID = 5832666152954738196L;

	private List<HResource> children = new ArrayList<HResource>();
	
	public HParentResource() {
	}
	
	public HParentResource(Resource res) {
		super(res);
	}

	@OneToMany(mappedBy="parent", cascade = CascadeType.ALL)
	@IndexColumn(name = "pos", base=0)
	@Where(clause="obsolete=0")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<HResource> getChildren() {
		return children;
	}
	
	public void setChildren(List<HResource> children) {
		this.children = children;
	}
	
}
