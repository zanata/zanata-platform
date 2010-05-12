package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="sourceResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class SourceResource extends AbstractContentTranslationResource<SourceTextFlow> {

	public SourceResource() {
	}

	public SourceResource(String id, String name) {
		super(id, name);
	}
	
	
}
