package org.fedorahosted.flies.rest.dto;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

class Utility {

	public static String toXML(Object obj) {
		try {
			Marshaller m = null;
			JAXBContext jc = JAXBContext.newInstance(obj.getClass());
			m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter writer = new StringWriter();
			m.marshal(obj, writer);
			return writer.toString();
		} catch (Exception e) {
			return String.valueOf(obj);
		}
	}

}
