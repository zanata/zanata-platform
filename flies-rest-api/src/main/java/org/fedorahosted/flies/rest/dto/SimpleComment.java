package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;



@XmlRootElement(name="comment", namespace=Namespaces.FLIES)
@XmlType(name="simpleCommentType", namespace=Namespaces.FLIES)
public class SimpleComment {
	
	private String id;
	private String value;

	public SimpleComment() {
	}
	
	public SimpleComment(String id, String value) {
		this.id = id;
		this.value = value;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	@XmlValue
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@XmlAttribute(name="space", namespace=Namespaces.XML)
	public String getSpace() {
		return "preserve";
	}
	
	public void setSpace(String space) {
	}
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
