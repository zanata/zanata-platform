package org.fedorahosted.flies.webtrans.gwt;

import org.fedorahosted.flies.gwt.common.MyService;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;

@Name("org.fedorahosted.flies.gwt.common.MyService")
public class ServiceImpl implements MyService {

	@WebRemote
	public String askIt(String question) {
		if (!validate(question)) {
			throw new IllegalStateException(
					"Hey, this shouldn't happen, I checked on the client, "
							+ "but its always good to double check.");
		}

		return "42. Its the real question that you seek now.";
	}

	public boolean validate(String q) {
		return true;
	}

}