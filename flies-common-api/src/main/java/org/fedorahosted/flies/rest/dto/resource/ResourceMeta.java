package org.fedorahosted.flies.rest.dto.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.rest.dto.HasCollectionSample;


@XmlType(name="resourceMetaType", namespace=Namespaces.FLIES)
@XmlRootElement(name="resource-meta", namespace=Namespaces.FLIES)
public class ResourceMeta extends AbstractResourceMeta implements HasCollectionSample<ResourceMeta> {
	
	public ResourceMeta() {
	}
	
	public ResourceMeta(String resId) {
		super(resId);
	}
	
	@Override
	public ResourceMeta createSample() {
		ResourceMeta entity = new ResourceMeta();
		entity.setContentType(ContentType.TextPlain);
		entity.setName("readme.txt");
		entity.setLang(LocaleId.EN);
		entity.setType(ResourceType.FILE);
		// TODO add sample extension
		return entity;
	}
	
	@Override
	public Collection<ResourceMeta> createSamples() {
		List<ResourceMeta> elems = new ArrayList<ResourceMeta>(2);
		elems.add(createSample());
		ResourceMeta sample2 = createSample();
		sample2.setName("license.txt");
		elems.add(sample2);
		return elems;
	}

}
