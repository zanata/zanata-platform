package org.fedorahosted.flies.rest.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.MediaTypes;


@XmlType(name="documentViewType", namespace=Namespaces.DOCUMENT, propOrder={"links", "resources", "extensions"})
@XmlRootElement(name="document", namespace=Namespaces.DOCUMENT)
public class DocumentView extends Document{

	private Set<LocaleId> targetLanguages;
	
	private DocumentView(){
		super();
	}
	
	public DocumentView(Document doc) {
		super(doc);
	}

	public DocumentView(DocumentView other) {
		super(other);
		if(other.targetLanguages != null) {
			this.targetLanguages = new HashSet<LocaleId>(other.targetLanguages);
		}
	}

	/**
	 * This field is only used in GET requests, and is ignored in other operation
	 * 
	 * @return Set of available target-language translations for this Document
	 */
	@XmlAttribute(name="target-languages", required=false)
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	public Set<LocaleId> getTargetLanguages() {
		if(targetLanguages == null)
			targetLanguages = new HashSet<LocaleId>();
		return targetLanguages;
	}
	
	
	public void setSelfRef(URI self){
		Link link = findLinkByType(Relationships.SELF);
		if(link == null){
			link = new Link(self, Relationships.SELF, MediaTypes.APPLICATION_FLIES_DOCUMENT_RESOURCE_XML);
			getLinks().add(link);
		}
		else{
			link.setHref(self);
		}
	}
	
	public void setContainerRef(URI container){
		Link link = findLinkByType(Relationships.DOCUMENT_CONTAINER);
		if(link == null){
			link = new Link(container, Relationships.DOCUMENT_CONTAINER, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML);
			getLinks().add(link);
		}
		else{
			link.setHref(container);
		}
	}
	
}
