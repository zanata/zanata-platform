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

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
public class DocumentTarget implements Serializable{

    private DocumentTargetId id;
    
    private Integer version;
    
	private List<TextUnitTarget> entries;

	@Id
	public DocumentTargetId getId() {
		return id;
	}	
	
	public void setId(DocumentTargetId id) {
		this.id = id;
	}
	
    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }
	
	@OneToMany(mappedBy="documentTarget")
	public List<TextUnitTarget> getEntries() {
		return entries;
	}
	
	public void setEntries(List<TextUnitTarget> entries) {
		this.entries = entries;
	}
	
}
