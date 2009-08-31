package org.fedorahosted.flies.client.ant.properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;

class Context {
    static JAXBContext newJAXBContext() throws JAXBException {
	JAXBContext jc = JAXBContext.newInstance(Project.class.getPackage().getName()+":"+TextFlowTargets.class.getPackage().getName());
//	JAXBContext jc = JAXBContext.newInstance(Project.class, Document.class, TextFlowTargets.class);
	return jc;
    }
}
