package org.fedorahosted.flies.rest.dto.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="translationResourceType", namespace=Namespaces.FLIES, propOrder={"extensions", "textFlows"})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class TargetResource implements Serializable {

	private ExtensionSet extensions;
	private List<MultiTargetTextFlow> textFlows;
	
	public TargetResource() {
	}

	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<MultiTargetTextFlow> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<MultiTargetTextFlow>();
		}
		return textFlows;
	}
	
	@XmlElementWrapper(name="extensions", namespace=Namespaces.FLIES, required=false, nillable=false)
	public ExtensionSet getExtensions() {
		if(extensions == null)
			extensions = new ExtensionSet();
		return extensions;
	}
	
}
