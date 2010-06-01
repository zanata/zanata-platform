package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.hibernate.validator.NotEmpty;

@XmlType(name="textFlowTargetWithIdType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="target", namespace=Namespaces.FLIES)
public class TextFlowTargetWithId extends TextFlowTarget {
	
	private String resId;
	
	public TextFlowTargetWithId() {
	}

	public TextFlowTargetWithId(String resId) {
		this.resId = resId;
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
