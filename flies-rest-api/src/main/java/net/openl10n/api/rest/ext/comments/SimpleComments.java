package net.openl10n.api.rest.ext.comments;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="simple-comments", namespace=SimpleComment.NAMESPACE)
public class SimpleComments {

	private List<SimpleComment> comments;
	
	@XmlElement(name="comment", namespace=SimpleComment.NAMESPACE)
	public List<SimpleComment> getComments() {
		if(comments == null)
			comments = new ArrayList<SimpleComment>();
		return comments;
	}
	
}
