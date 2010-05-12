package org.fedorahosted.flies.rest.dto.v1;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="sourceAsTargetTextFlowType", namespace=Namespaces.FLIES, propOrder={"translator"})
@XmlRootElement(name="text-flow", namespace=Namespaces.FLIES)
public class SourceAsTargetTextFlow extends AbstractTextFlow {

	private Person translator;
	
	public SourceAsTargetTextFlow() {
	}
	
	public SourceAsTargetTextFlow(String id) {
		super(id);
	}
	
	public SourceAsTargetTextFlow(String id, LocaleId lang) {
		super(id, lang);
	}
	
	public SourceAsTargetTextFlow(String id, LocaleId lang, String content) {
		super(id, lang, content);
	}
	
	@XmlElement(name="translator", namespace=Namespaces.FLIES, required=true)
	public Person getTranslator() {
		return translator;
	}
	
	public void setTranslator(Person translator) {
		this.translator = translator;
	}
	
}
