package org.fedorahosted.flies.adapter.po;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.rest.dto.SimpleComment;


@XmlType(name="headerType", namespace=PoHeader.NAMESPACE, propOrder={"comment", "entries"})
@XmlRootElement(name="header", namespace=PoHeader.NAMESPACE)
public class PoHeader {
	
	public static final String NAMESPACE = "http://flies.fedorahosted.org/api/gettext/header";
	public static final String NAMESPACE_JSON = "poheader";
	
	private SimpleComment comment;
	private List<HeaderEntry> entries;
	
	@XmlElement(name="comment", namespace=NAMESPACE, required=true)
	public SimpleComment getComment() {
		if(comment == null)
			comment = new SimpleComment();
		return comment;
	}

	public void setComment(SimpleComment comment) {
		this.comment = comment;
	}
	
	public void setComment(String comment){
		getComment().setValue(comment);
	}
	
	@XmlElementWrapper(name="entries", namespace=NAMESPACE, required=true)
	@XmlElement(name="entry", namespace=NAMESPACE)
	public List<HeaderEntry> getEntries() {
		if(entries == null)
			entries = new ArrayList<HeaderEntry>();
		return entries;
	}	
	
}
