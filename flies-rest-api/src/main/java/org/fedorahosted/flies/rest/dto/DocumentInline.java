package org.fedorahosted.flies.rest.dto;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlType(name="documentRefType", namespace=Namespaces.DOCUMENT)
public class DocumentInline extends AbstractDocument{

	private URI ref;

	private DocumentInline(){
		super();
	}
	
	public DocumentInline(Document doc) {
		super(doc);
	}
	
	@XmlAttribute
	public URI getRef() {
		return ref;
	}
	
	public void setRef(URI ref) {
		this.ref = ref;
	}
}
