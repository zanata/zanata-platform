package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="abstractTranslationResourceType", namespace=Namespaces.FLIES, propOrder={"textFlows"})
public abstract class AbstractContentTranslationResource<T> extends AbstractTranslationResource{

	private List<T> textFlows;
	
	public AbstractContentTranslationResource() {
	}

	public AbstractContentTranslationResource(String name) {
		super(name);
	}
	
	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<T> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<T>();
		}
		return textFlows;
	}
	

}
