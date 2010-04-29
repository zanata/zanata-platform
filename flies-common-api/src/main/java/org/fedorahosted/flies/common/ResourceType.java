package org.fedorahosted.flies.common;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="resourceType", namespace=Namespaces.FLIES)
public enum ResourceType {
	FILE, DOCUMENT, PAGE;
}
