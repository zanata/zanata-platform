package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.fedorahosted.flies.entity.FliesLocale;

@Entity
@Table(	uniqueConstraints = {@UniqueConstraint(columnNames={"resource_id", "document_id"})})
public class TextUnit extends AbstractTextUnitTemplate{

	private List<TextUnitTarget> targets;
	
	@OneToMany(mappedBy="template")
	public List<TextUnitTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextUnitTarget> targets) {
		this.targets = targets;
	}

}
