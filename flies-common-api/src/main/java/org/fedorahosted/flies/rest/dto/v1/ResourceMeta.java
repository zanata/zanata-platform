package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="resourceMetaType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource", namespace=Namespaces.FLIES)
public class ResourceMeta extends AbstractResource {
	
	public ResourceMeta() {
	}
	
	public ResourceMeta(String name) {
		super(name);
	}
	

}
