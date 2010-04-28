package org.fedorahosted.flies.rest.dto.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.rest.dto.ContentTypeAdapter;
import org.fedorahosted.flies.rest.dto.LocaleIdAdapter;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;

@XmlType(name="abstractTranslationResourceType", namespace=Namespaces.FLIES, propOrder={"textFlows"})
public abstract class AbstractContentTranslationResource<T> extends AbstractTranslationResource{

	private List<T> textFlows;
	
	public AbstractContentTranslationResource() {
	}

	public AbstractContentTranslationResource(String id) {
		super(id);
	}
	
	@XmlElementWrapper(name="text-flows", namespace=Namespaces.FLIES, required=false)
	public List<T> getTextFlows() {
		if(textFlows == null) {
			textFlows = new ArrayList<T>();
		}
		return textFlows;
	}
	

}
