package org.fedorahosted.flies.repository.model.po;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HSimpleComment;

/**
 * 
 * @author sflaniga@redhat.com
 * @see org.fedorahosted.flies.rest.dto.po.PoHeader
 */
@MappedSuperclass
public abstract class AbstractPoHeader implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private HSimpleComment comment;
	private String entries;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	public abstract void setDocument(HDocument document);

	@Transient
	public abstract HDocument getDocument();
	
	public void setComment(HSimpleComment comment) {
		this.comment = comment;
	}

	@OneToOne(optional=true, cascade=CascadeType.ALL)
	@JoinColumn(name="comment_id")
	public HSimpleComment getComment() {
		return comment;
	}

	// stored in the format used by java.util.Properties.store(Writer)
	// see PoUtility.headerEntriesToString
	public void setEntries(String entries) {
		this.entries = entries;
	}

	// see PoUtility.stringToHeaderEntries
	public String getEntries() {
		return entries;
	}

	/**
	 * Used for debugging
	 */
	@Override
	public String toString() {
		return 
			"document:"+getDocument()+
			"comment:"+getComment()+
			"entries:"+getEntries()+
			"";
	}
}
