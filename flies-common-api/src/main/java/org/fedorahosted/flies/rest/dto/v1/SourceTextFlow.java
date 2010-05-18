package org.fedorahosted.flies.rest.dto.v1;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;


@XmlType(name="sourceTextFlowType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="text-flow", namespace=Namespaces.FLIES)
public class SourceTextFlow extends AbstractTextFlow {

	public SourceTextFlow() {
	}
	
	public SourceTextFlow(String id) {
		super(id);
	}
	
	public SourceTextFlow(String id, LocaleId lang) {
		super(id, lang);
	}
	
	public SourceTextFlow(String id, LocaleId lang, String content) {
		super(id, lang, content);
	}
	
}
