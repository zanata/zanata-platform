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

	private Long id;
    private Integer version;

	private Document template;
	private List<TextUnitTarget> entries;

	private Locale locale;
	
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
	public Document getTemplate() {
		return template;
	}
	
	public void setTemplate(Document template) {
		this.template = template;
	}
	
	@OneToMany(mappedBy="documentTarget")
	public List<TextUnitTarget> getEntries() {
		return entries;
	}
	
	public void setEntries(List<TextUnitTarget> entries) {
		this.entries = entries;
	}
	
	
	@ManyToOne
	@JoinColumn(name="locale_id")
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
}
