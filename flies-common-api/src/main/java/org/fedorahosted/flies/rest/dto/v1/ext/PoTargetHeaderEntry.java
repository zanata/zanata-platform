package org.fedorahosted.flies.rest.dto.v1.ext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;

@XmlType(name="poTargetHeaderEntry", namespace=PoHeader.NAMESPACE, propOrder={"comment", "entries"})
@XmlRootElement(name="po-target-header", namespace=PoHeader.NAMESPACE)
public class PoTargetHeaderEntry {

	private LocaleId locale;
	
	private String comment;
	private List<HeaderEntry> entries;
	
	public PoTargetHeaderEntry() {
	}
	
	public PoTargetHeaderEntry(LocaleId locale, String comment, HeaderEntry ... entries) {
		this.locale = locale;
		setComment(comment);
		for (int i = 0; i < entries.length; i++) {
			getEntries().add(entries[i]);
		}
	}
	
	@XmlAttribute(name="locale", required=true)
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@XmlElement(name="comment", namespace=PoHeader.NAMESPACE, required=true)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@XmlElementWrapper(name="entries", namespace=PoHeader.NAMESPACE, required=true)
	@XmlElement(name="entry", namespace=PoHeader.NAMESPACE)
	public List<HeaderEntry> getEntries() {
		if(entries == null)
			entries = new ArrayList<HeaderEntry>();
		return entries;
	}	
	
}
