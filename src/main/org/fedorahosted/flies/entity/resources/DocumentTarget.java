package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class DocumentTarget implements Serializable{

	private Long id;
    private Integer version;

	private DocumentTemplate template;
	private List<TextFlowTarget> entries;

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
	}
	
    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }
	
	@ManyToOne
	@JoinColumn(name="template_id")
	public DocumentTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(DocumentTemplate template) {
		this.template = template;
	}
	
	@OneToMany(mappedBy="documentTarget")
	public List<TextFlowTarget> getEntries() {
		return entries;
	}
	
	public void setEntries(List<TextFlowTarget> entries) {
		this.entries = entries;
	}
	
}
