package org.fedorahosted.flies.rest.dto.v1;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.HasSample;

@XmlType(name="resourcesListType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resourcesListType", namespace=Namespaces.FLIES)
public class ResourcesList implements Serializable, HasSample<ResourcesList>{
	
	private List<ResourceMeta> resources;
	
	@XmlElement(name="resource", namespace=Namespaces.FLIES)
	public List<ResourceMeta> getResources() {
		if(resources == null) {
			resources = new ArrayList<ResourceMeta>();
		}
		return resources;
	}
	@Override
	public ResourcesList createSample() {
		ResourcesList entity = new ResourcesList();
		entity.getResources().addAll(new ResourceMeta().createSamples());
		return entity;
	}
}
