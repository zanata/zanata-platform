package org.fedorahosted.flies.rest.dto.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.Person;
import org.hibernate.validator.NotEmpty;

@XmlType(name="textFlowTargetType", namespace=Namespaces.FLIES, propOrder={"translator", "content", "extensions"})
@XmlRootElement(name="text-flow-target", namespace=Namespaces.FLIES)
@JsonPropertyOrder({"resId", "state","translator", "content", "extensions"})
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonWriteNullProperties(false)
public class TextFlowTarget implements Serializable {

	private String resId;
	private ContentState state = ContentState.New;
	private Person translator;
	private String content;
	private ExtensionSet extensions;
	
	public TextFlowTarget() {
	}
	
	public TextFlowTarget(String resId) {
		this.resId = resId;
	}
	
	@XmlElementRef
	public Person getTranslator() {
		return translator;
	}
	
	public void setTranslator(Person translator) {
		this.translator = translator;
	}
	
	@XmlAttribute(name="state", required=true)
	public ContentState getState() {
		return state;
	}
	
	public void setState(ContentState state) {
		this.state = state;
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
		return extensions;
	}
	
	@JsonIgnore
	public ExtensionSet getExtensions(boolean createIfNull) {
		if(createIfNull && extensions == null)
			extensions = new ExtensionSet();
		return extensions;
	}
	
	public void setExtensions(ExtensionSet extensions) {
		this.extensions = extensions;
	}
	
	@XmlAttribute(name="res-id", required=true)
	@NotEmpty
	public String getResId() {
		return resId;
	}
	
	public void setResId(String resId) {
		this.resId = resId;
	}
	
	
}
