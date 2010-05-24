package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="translationResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource", namespace=Namespaces.FLIES)
public class TranslationResource extends AbstractTranslationResource {
	
	public TranslationResource() {
	}
	
	public TranslationResource(String name) {
		super(name);
	}
	

}
