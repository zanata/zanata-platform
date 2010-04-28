package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="sourceAsTargetResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class SourceAsTargetResource extends AbstractContentTranslationResource<SourceAsTargetTextFlow> {

	public SourceAsTargetResource() {
	}

	public SourceAsTargetResource(String id) {
		super(id);
	}
	
}
