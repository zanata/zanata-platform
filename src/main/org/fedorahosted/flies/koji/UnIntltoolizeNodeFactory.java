package org.fedorahosted.flies.koji;

import nu.xom.Element;
import nu.xom.NodeFactory;

public class UnIntltoolizeNodeFactory extends NodeFactory{

	@Override
	public Element startMakingElement(String name, String namespace) {
		if(name.startsWith("_")){
			return super.startMakingElement(name.substring(1), namespace);
		}
		return super.startMakingElement(name, namespace);
	}

}
