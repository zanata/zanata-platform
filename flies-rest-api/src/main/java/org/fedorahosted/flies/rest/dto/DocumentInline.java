package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlType(name="documentRefType", namespace=Namespaces.DOCUMENT)
public class DocumentInline extends AbstractDocument{

	private Document ref;

	private DocumentInline(){
		super();
	}
	
	public DocumentInline(Document doc) {
		super(doc);
		this.ref = doc;
	}
	
	@XmlJavaTypeAdapter(value = UriAdapter.class)
	@XmlAttribute
	public Document getRef() {
		return ref;
	}
	
	public void setRef(Document ref) {
		this.ref = ref;
	}
}
