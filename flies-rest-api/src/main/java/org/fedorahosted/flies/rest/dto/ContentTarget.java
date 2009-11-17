package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.common.LocaleId;


@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
@XmlRootElement(name="document-target", namespace=Namespaces.FLIES)
public class ContentTarget {
	
	private LocaleId targetLanguage;

	private List<TextFlowTarget> textFlowTargets;

	@XmlAttribute(name="target-language", required=true)
	public LocaleId getTargetLanguage() {
		return targetLanguage;
	}
	
	public void setTargetLanguage(LocaleId targetLanguage) {
		this.targetLanguage = targetLanguage;
	}
	
	@XmlElement(name="text-flow-target", namespace=Namespaces.FLIES)
	public List<TextFlowTarget> getTextFlowTargets() {
		return textFlowTargets;
	}

	public List<TextFlowTarget> getTextFlowTargets(boolean create) {
		if(textFlowTargets == null && create)
			textFlowTargets = new ArrayList<TextFlowTarget>();
		return textFlowTargets;
	}

	public boolean hasTextFlowTargets() {
		return textFlowTargets != null;
	}
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
