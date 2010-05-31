package org.fedorahosted.flies.rest.dto.v1.ext;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.flies.rest.dto.v1.Extension;

@XmlType(name="poHeaderExtension", namespace=PoHeader.NAMESPACE, propOrder={"comment", "entries"})
@XmlRootElement(name="po-header", namespace=PoHeader.NAMESPACE)
@JsonTypeName(value=PoHeader.ID)
public class PoHeader extends Extension {
	
	public static final String ID = "gettext-pot-header";
	public static final String VERSION = "1.0";
	public static final String NAMESPACE = "http://flies.fedorahosted.org/api/gettext/header";

	private String comment;
	private List<HeaderEntry> entries;
	
	public PoHeader() {
		super(ID, VERSION);
	}
	
	public PoHeader(String comment, HeaderEntry ... entries) {
		this();
		setComment(comment);
		for (int i = 0; i < entries.length; i++) {
			getEntries().add(entries[i]);
		}
	}
	
	@XmlElement(name="comment", namespace=NAMESPACE, required=true)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@XmlElementWrapper(name="entries", namespace=NAMESPACE, required=true)
	@XmlElement(name="entry", namespace=NAMESPACE)
	public List<HeaderEntry> getEntries() {
		if(entries == null)
			entries = new ArrayList<HeaderEntry>();
		return entries;
	}	
	
}
