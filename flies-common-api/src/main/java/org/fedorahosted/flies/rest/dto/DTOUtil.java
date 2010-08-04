package org.fedorahosted.flies.rest.dto;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTOUtil
{
   private final static Logger log = LoggerFactory.getLogger(DTOUtil.class);

   public static String toXML(Object obj)
   {
      try
      {
         Marshaller m = null;
         JAXBContext jc = JAXBContext.newInstance(obj.getClass());
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         m.setProperty(Marshaller.JAXB_FRAGMENT, true);
         StringWriter writer = new StringWriter();
         m.marshal(obj, writer);
         return writer.toString();
      }
      catch (Exception e)
      {
         log.error("toXML failed", e);
         return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
      }
   }

}
