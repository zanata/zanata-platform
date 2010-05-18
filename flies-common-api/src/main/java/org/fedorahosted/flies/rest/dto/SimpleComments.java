package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fedorahosted.flies.common.Namespaces;

@XmlRootElement(name="simple-comments", namespace=Namespaces.FLIES)
public class SimpleComments {

	private List<SimpleComment> comments;
	
	@XmlElement(name="comment", namespace=Namespaces.FLIES)
	public List<SimpleComment> getComments() {
		if(comments == null)
			comments = new ArrayList<SimpleComment>();
		return comments;
	}
	
	@Override
	public String toString() {
		return Utility.toXML(this);
	}
	
}
