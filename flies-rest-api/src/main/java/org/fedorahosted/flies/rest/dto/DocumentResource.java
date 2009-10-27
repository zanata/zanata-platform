package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;



@XmlType(name="resourceType", namespace=Namespaces.FLIES)
@XmlSeeAlso({TextFlow.class, Container.class, Reference.class})
public interface DocumentResource {

	public String getId();
	
	public Integer getRevision();

}
