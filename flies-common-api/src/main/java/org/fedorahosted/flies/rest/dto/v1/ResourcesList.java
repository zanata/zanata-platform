package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="resourcesListType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resources", namespace=Namespaces.FLIES)
public class ResourcesList extends ArrayList<TranslationResource> {
	
}
