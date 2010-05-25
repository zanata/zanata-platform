package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="targetResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class TargetResource extends AbstractContentTranslationResource<MultiTargetTextFlow> {

	public TargetResource() {
	}

	public TargetResource(String name) {
		super(name);
	}

	@Override
	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<MultiTargetTextFlow> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<MultiTargetTextFlow>();
		}
		return textFlows;
	}
	
}
