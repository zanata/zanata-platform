package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.LocaleIdAdapter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@XmlType(name="abstractTextFlowType", namespace=Namespaces.FLIES, propOrder={"content", "extensions"})
public abstract class AbstractTextFlow {
	
	@NotEmpty @Length(max=255)
	private String id;
	
	@NotNull
	private LocaleId lang;
	
	@NotNull
	private String content;

	private ExtensionSet extensions;
	
	public AbstractTextFlow() {
	}
	
	/**
	 * This constructor sets the lang value to en-US
	 * 
	 * @param id Resource Id value
	 */
	public AbstractTextFlow(String id) {
		this.id = id;
		this.lang = LocaleId.EN_US;
	}
	
	public AbstractTextFlow(String id, LocaleId lang) {
		this(id);
		this.lang = lang;
	}

	public AbstractTextFlow(String id, LocaleId lang, String content) {
		this(id, lang);
		this.content = content;
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

	@XmlElementWrapper(name="extensions", namespace=Namespaces.FLIES, required=false)
	@XmlAnyElement(lax=true)
	public ExtensionSet getExtensions() {
		if(extensions == null)
			extensions = new ExtensionSet();
		return extensions;
	}
	
}
