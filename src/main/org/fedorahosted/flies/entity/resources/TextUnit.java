package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
public class TextUnit extends AbstractTextUnit{

	private TextUnitId id;
	
	private List<TextUnitTarget> targets;
	
	private boolean obsolete;
	
	private Integer position;
	
	@Id
	public TextUnitId getId() {
		return id;
	}
	
	public void setId(TextUnitId id) {
		this.id = id;
	}
	
	@OneToMany(mappedBy="id.template")
	public List<TextUnitTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextUnitTarget> targets) {
		this.targets = targets;
	}

	public boolean isObsolete() {
		return obsolete;
	}
	
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Integer getPosition() {
		return position;
	}
	
	public void setPosition(Integer position) {
		this.position = position;
	}
	
}
