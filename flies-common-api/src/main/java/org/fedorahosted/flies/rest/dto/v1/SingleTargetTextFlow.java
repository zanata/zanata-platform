package org.fedorahosted.flies.rest.dto.v1;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="singleTargetTextFlowType", namespace=Namespaces.FLIES, propOrder={"target"})
@XmlRootElement(name="text-flow", namespace=Namespaces.FLIES)
public class SingleTargetTextFlow extends AbstractTextFlow {

	private TextFlowTarget target;
	
	public SingleTargetTextFlow() {
	}
	
	public SingleTargetTextFlow(String id) {
		super(id);
	}
	
	public SingleTargetTextFlow(String id, LocaleId lang) {
		super(id, lang);
	}
	
	public SingleTargetTextFlow(String id, LocaleId lang, String content) {
		super(id, lang, content);
	}

	@XmlElement(name="target", namespace=Namespaces.FLIES, required=true)
	public TextFlowTarget getTarget() {
		return target;
	}
	
	public void setTarget(TextFlowTarget target) {
		this.target = target;
	}
	
}
