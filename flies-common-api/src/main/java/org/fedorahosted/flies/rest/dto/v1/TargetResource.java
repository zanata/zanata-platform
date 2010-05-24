package org.fedorahosted.flies.rest.dto.v1;

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
	
}
