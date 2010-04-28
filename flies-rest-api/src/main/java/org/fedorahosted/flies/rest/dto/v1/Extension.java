package org.fedorahosted.flies.rest.dto.v1;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;

@XmlType(name="extensionType", namespace=Namespaces.FLIES)
@XmlSeeAlso({PoHeader.class})
public abstract class Extension {

	private final String id;
	
	public Extension(String id) {
		this.id = id;
	}
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}

}
