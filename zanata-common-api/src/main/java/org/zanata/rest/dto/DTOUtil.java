package org.zanata.rest.dto;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTOUtil
{
   private final static Logger log = LoggerFactory.getLogger(DTOUtil.class);

   @SuppressWarnings({ "unchecked" })
   public static <T> String toXML(T obj)
   {
      try
      {
         Marshaller m = null;
         JAXBContext jc = JAXBContext.newInstance(obj.getClass());
         m = jc.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         m.setProperty(Marshaller.JAXB_FRAGMENT, true);
         StringWriter writer = new StringWriter();
         
         /* Marshal objects into a string differently depending on whether they are root elements
            or not */
         if( obj.getClass().getAnnotation(XmlRootElement.class) != null )
         {
            m.marshal(obj, writer);
         }
         else
         {
            m.marshal(new JAXBElement<T>(new QName( "", obj.getClass().getSimpleName() ), (Class<T>)obj.getClass(), obj), writer);
         }
         return writer.toString();
      }
      catch (Exception e)
      {
         log.error("toXML failed", e);
         return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
      }
   }

}
