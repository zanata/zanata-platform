package org.fedorahosted.flies.adapter.po;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.openl10n.api.LocaleId;
import net.openl10n.api.LocaleIdAdapter;

@XmlType(name="poTargetHeaderType", namespace=PoHeader.NAMESPACE)
@XmlRootElement(name="po-target-header", namespace=PoHeader.NAMESPACE)
public class PoTargetHeader extends PoHeader{
	
	private LocaleId targetLanguage;
	
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	@XmlAttribute(name="target-language", required=true)
	public LocaleId getTargetLanguage() {
		return targetLanguage;
	}
	
	public void setTargetLanguage(LocaleId targetLanguage) {
		this.targetLanguage = targetLanguage;
	}

}
