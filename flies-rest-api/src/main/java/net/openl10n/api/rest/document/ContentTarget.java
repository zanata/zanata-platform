package net.openl10n.api.rest.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.api.LocaleId;
import net.openl10n.api.LocaleIdAdapter;
import net.openl10n.api.Namespaces;

@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
@XmlRootElement(name="document-target", namespace=Namespaces.DOCUMENT)
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
	
	@XmlElement(name="text-flow-target", namespace=Namespaces.DOCUMENT)
	public List<TextFlowTarget> getTextFlowTargets() {
		if(textFlowTargets == null)
			textFlowTargets = new ArrayList<TextFlowTarget>();
		return textFlowTargets;
	}
}
