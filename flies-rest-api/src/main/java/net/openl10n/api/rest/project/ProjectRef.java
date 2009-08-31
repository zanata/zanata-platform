package net.openl10n.api.rest.project;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.Namespaces;

import net.openl10n.api.rest.UriAdapter;

@XmlRootElement(name="project", namespace=Namespaces.DOCUMENT)
@XmlType(name="projectRefType", namespace=Namespaces.DOCUMENT)
public class ProjectRef extends AbstractProject{
	
	private Project ref;
	
	// private enum ProjecType type; 
	
	private ProjectRef(){
		super();
	}
	
	public ProjectRef(Project project) {
		super(project);
		this.ref = project;
	}
	
	@XmlJavaTypeAdapter(value = UriAdapter.class)
	@XmlAttribute
	public Project getRef() {
		return ref;
	}
	
	public void setRef(Project ref) {
		this.ref = ref;
	}
}
