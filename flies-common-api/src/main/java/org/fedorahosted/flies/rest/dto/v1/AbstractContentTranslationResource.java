package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="abstractTranslationResourceType", namespace=Namespaces.FLIES, propOrder={"textFlows"})
public abstract class AbstractContentTranslationResource<T> extends AbstractResource{

	protected List<T> textFlows;
	
	public AbstractContentTranslationResource() {
	}

	public AbstractContentTranslationResource(String name) {
		super(name);
	}
	
	public abstract List<T> getTextFlows();
	

}
