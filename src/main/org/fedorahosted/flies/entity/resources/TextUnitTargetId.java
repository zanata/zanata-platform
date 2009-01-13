package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.fedorahosted.flies.entity.locale.Locale;

@Embeddable
public class TextUnitTargetId implements Serializable{

	private TextUnit template;
	private Locale locale;

	@ManyToOne
	@JoinColumns({
	    @JoinColumn(name="resource_id", referencedColumnName="resourceId"),
	    @JoinColumn(name="document_id", referencedColumnName="document_id")
	})
	public TextUnit getTemplate() {
		return template;
	}
	
	public void setTemplate(TextUnit template) {
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
		
		final TextUnitTargetId other = (TextUnitTargetId) obj;

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
