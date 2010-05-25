package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="sourceResourceType", namespace=Namespaces.FLIES, propOrder={})
@XmlRootElement(name="resource",namespace=Namespaces.FLIES)
public class SourceResource extends AbstractContentTranslationResource<SourceTextFlow> {

	public SourceResource() {
	}

	public SourceResource(String name) {
		super(name);
	}
	
	@Override
	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<SourceTextFlow> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<SourceTextFlow>();
		}
		return textFlows;
	}
	
	
}
