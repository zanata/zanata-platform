package org.fedorahosted.flies.rest.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;



@XmlType(name="resourceType", namespace=Namespaces.DOCUMENT)
@XmlSeeAlso({TextFlow.class, Container.class, Reference.class})
public interface Resource extends IExtensible{

	public String getId();
	
	public long getVersion();

	public List<Object> getExtensions();

}
