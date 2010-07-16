package org.fedorahosted.flies.rest.dto.deprecated;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Utility
{

   final static Logger logger = LoggerFactory.getLogger(Utility.class);

   public static String toXML(Object obj)
   {
      try
      {
         Marshaller m = null;
         JAXBContext jc = JAXBContext.newInstance(obj.getClass());
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         StringWriter writer = new StringWriter();
         m.marshal(obj, writer);
         return writer.toString();
      }
      catch (Exception e)
      {
         logger.error("toXML failed", e);
         return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
      }
   }

}
