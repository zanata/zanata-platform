package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue("folder")
public class Folder extends ContainerItem{
	
	private List<ContainerItem> children = new ArrayList<ContainerItem>();
	
	@OneToMany(mappedBy = "parent")
	@IndexColumn(name="pos")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<ContainerItem> getChildren() {
		return children;
	}
	
	public void setChildren(List<ContainerItem> children) {
		this.children = children;
	}
	
}
