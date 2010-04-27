package org.fedorahosted.flies.rest.dto.v1;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.LocaleIdAdapter;


@XmlType(name="textFlowType", namespace=Namespaces.FLIES, propOrder={"content"})
@XmlRootElement(name="text-flow", namespace=Namespaces.FLIES)
public class TextFlow  {

	private String id;
	private LocaleId lang;
	
	private String content;

	public TextFlow() {
	}
	
	public TextFlow(String id) {
		this.id = id;
	}
	
	public TextFlow(String id, LocaleId lang) {
		this(id);
		this.lang = lang;
	}

	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	@XmlAttribute(name="lang", namespace=Namespaces.XML, required=false)
	public LocaleId getLang() {
		return lang;
	}
	
	public void setLang(LocaleId lang) {
		this.lang = lang;
	}

	@XmlElement(name="content",namespace=Namespaces.FLIES, required=true)
	public String getContent() {
		if(content == null)
			return "";
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
}
