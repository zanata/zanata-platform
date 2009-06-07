package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class ProjectContainer extends AbstractFliesEntity{

	private List<ContainerItem> items = new ArrayList<ContainerItem>();
	
	@IndexColumn(name="pos")
	@OneToMany(mappedBy="container",cascade=CascadeType.ALL)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<ContainerItem> getItems() {
		return items;
	}
	
	public void setItems(List<ContainerItem> items) {
		this.items = items;
	}
}
