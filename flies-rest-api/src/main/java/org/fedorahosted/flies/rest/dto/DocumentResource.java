package org.fedorahosted.flies.rest.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;



@XmlType(name="resourceType", namespace=Namespaces.FLIES)
@XmlSeeAlso({TextFlow.class, Container.class, Reference.class})
public interface DocumentResource extends IExtensible{

	public String getId();
	
	public Integer getRevision();

	public List<Object> getExtensions();

}
