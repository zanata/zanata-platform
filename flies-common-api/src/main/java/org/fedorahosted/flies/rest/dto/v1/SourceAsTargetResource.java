package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="sourceAsTargetResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class SourceAsTargetResource extends AbstractContentTranslationResource<SourceAsTargetTextFlow> {

	public SourceAsTargetResource() {
	}

	public SourceAsTargetResource(String name) {
		super(name);
	}

	@Override
	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<SourceAsTargetTextFlow> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<SourceAsTargetTextFlow>();
		}
		return textFlows;
	}
	
}
