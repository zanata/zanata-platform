package org.fedorahosted.flies.rest.dto.v1;


import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="multiTargetTextFlowType", namespace=Namespaces.FLIES, propOrder={"targets"})
@XmlRootElement(name="text-flow", namespace=Namespaces.FLIES)
public class MultiTargetTextFlow extends AbstractTextFlow {

	private Map<LocaleId, TextFlowTarget> targets;
	
	public MultiTargetTextFlow() {
	}
	
	public MultiTargetTextFlow(String id) {
		super(id);
	}
	
	public MultiTargetTextFlow(String id, LocaleId lang) {
		super(id, lang);
	}
	
	public MultiTargetTextFlow(String id, LocaleId lang, String content) {
		super(id, lang, content);
	}

	@XmlElement(name="targets", namespace=Namespaces.FLIES, required=true)
	public Map<LocaleId, TextFlowTarget> getTargets() {
		if(targets == null)
			targets = new HashMap<LocaleId, TextFlowTarget>();
		return targets;
	}
	
}
