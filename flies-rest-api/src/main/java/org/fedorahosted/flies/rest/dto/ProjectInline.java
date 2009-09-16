package org.fedorahosted.flies.rest.dto;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlRootElement(name="project", namespace=Namespaces.DOCUMENT)
@XmlType(name="projectRefType", namespace=Namespaces.DOCUMENT)
public class ProjectInline extends AbstractProject{
	
	private URI ref;
	
	private ProjectInline(){
		super();
	}
	
	public ProjectInline(Project project) {
		super(project);
	}
	
	@XmlAttribute
	public URI getRef() {
		return ref;
	}
	
	public void setRef(URI ref) {
		this.ref = ref;
	}
}
