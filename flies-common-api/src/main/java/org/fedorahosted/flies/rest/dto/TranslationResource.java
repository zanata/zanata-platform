package org.fedorahosted.flies.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fedorahosted.flies.common.Namespaces;

@XmlType(name="translationResourceType", namespace=Namespaces.FLIES, propOrder={"extensions", "textFlowTargets"})
@XmlRootElement(name="translation-resource", namespace=Namespaces.FLIES)
public class TranslationResource implements Serializable, HasSample<TranslationResource> {

	private ExtensionSet extensions;
	private List<TextFlowTargetWithId> textFlowTargets;

	@XmlElementWrapper(name="extensions", namespace=Namespaces.FLIES, required=false, nillable=false)
	@XmlAnyElement(lax=true)
	public ExtensionSet getExtensions() {
		if(extensions == null)
			extensions = new ExtensionSet();
		return extensions;
	}

	@XmlElementWrapper(name="targets", namespace=Namespaces.FLIES, required=false)
	public List<TextFlowTargetWithId> getTextFlowTargets() {
		if(textFlowTargets == null) {
			textFlowTargets = new ArrayList<TextFlowTargetWithId>();
		}
		return textFlowTargets;
	}

	@Override
	public TranslationResource createSample() {
		return new TranslationResource();
	}	
}
