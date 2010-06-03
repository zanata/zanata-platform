package org.fedorahosted.flies.rest.dto.v1;

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
import org.fedorahosted.flies.rest.dto.HasSample;

@XmlType(name="resourceMetaType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource", namespace=Namespaces.FLIES)
public class ResourceMeta extends AbstractResource implements HasCollectionSample<ResourceMeta>{
	
	public ResourceMeta() {
	}
	
	public ResourceMeta(String name) {
		super(name);
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
