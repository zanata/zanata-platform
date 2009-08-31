package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.Namespaces;
import org.fedorahosted.flies.UriAdapter;


@XmlRootElement(name="project", namespace=Namespaces.DOCUMENT)
@XmlType(name="projectRefType", namespace=Namespaces.DOCUMENT)
public class ProjectIterationRef extends AbstractProject{
	
	private ProjectIteration ref;
	
	// private enum ProjecType type; 
	
	private ProjectIterationRef(){
		super();
	}
	
	public ProjectIterationRef(ProjectIteration project) {
		super(project);
		this.ref = project;
	}
	
	@XmlJavaTypeAdapter(value = UriAdapter.class)
	@XmlAttribute
	public ProjectIteration getRef() {
		return ref;
	}
	
	public void setRef(ProjectIteration ref) {
		this.ref = ref;
	}
}
