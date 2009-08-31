package net.openl10n.packaging.jpa.comments;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import net.openl10n.packaging.jpa.document.HTextFlowTarget;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

@Entity
public class HSimpleComment {
	
	private Long id;
	
	private HTextFlowTarget target;
	private String comment;
	
	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	
	@OneToOne
	@JoinColumn(name="target_id")
	@NaturalId
	public HTextFlowTarget getTarget() {
		return target;
	}

	public void setTarget(HTextFlowTarget target) {
		this.target = target;
	}
	
	@NotNull
	@Type(type = "text")
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
}
