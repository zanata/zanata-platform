package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlRootElement(name="project-iteration", namespace=Namespaces.DOCUMENT)
@XmlType(name="projectIterationRefType", namespace=Namespaces.DOCUMENT)
public class ProjectIterationInline extends AbstractProjectIteration{
	
	private ProjectIteration ref;
	
	// private enum ProjecType type; 
	
	private ProjectIterationInline(){
		super();
	}
	
	public ProjectIterationInline(ProjectIteration project) {
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
