package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@MappedSuperclass
public abstract class ParentResource extends Resource{

	private static final long serialVersionUID = 5832666152954738196L;

	private List<Resource> children = new ArrayList<Resource>();
	

	@OneToMany(mappedBy="parent", cascade = CascadeType.ALL)
	@IndexColumn(name = "position", base=1)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<Resource> getChildren() {
		return children;
	}
	
	public void setChildren(List<Resource> children) {
		this.children = children;
	}
	
}
