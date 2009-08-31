package net.openl10n.api.rest.document;

import java.util.List;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.Namespaces;

import net.openl10n.api.rest.IExtensible;

@XmlType(name="resourceType", namespace=Namespaces.DOCUMENT)
@XmlSeeAlso({TextFlow.class, Container.class, Reference.class})
public interface Resource extends IExtensible{

	public String getId();
	
	public long getVersion();

	public List<Object> getExtensions();

}
