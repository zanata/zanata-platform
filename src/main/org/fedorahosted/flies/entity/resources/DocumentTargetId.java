package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.entity.locale.Locale;

@Embeddable
public class DocumentTargetId implements Serializable{
	
	private Document template;
	private Locale locale;
	
	public DocumentTargetId() {}
	
	public DocumentTargetId(Document template, Locale locale) {
		this.template = template;
		this.locale = locale;
	}
	
	@ManyToOne
	@JoinColumn(name="template_id")
	public Document getTemplate() {
		return template;
	}
	
	public void setTemplate(Document template) {
		this.template = template;
	}
	
	@ManyToOne
	@JoinColumn(name="locale_id")
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		
		final DocumentTargetId other = (DocumentTargetId) obj;

		if(template == null && other.template != null) return false;
		else if(!template.equals(other.template)) return false;
		
		if(locale == null && other.locale != null) return false;
		else if(!locale.equals(other.locale)) return false;
		
		return true;
		  		
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31; 
	    int result = 1; 
	    result = prime * result + ((template == null) ? 0 : template.hashCode()); 
	    result = prime * result + ((locale == null) ? 0 : locale.hashCode()); 
	    return result;  	
	}
}
