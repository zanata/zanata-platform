package org.fedorahosted.flies.rest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.ValidationException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JaxbUtil
{
   public static void validateXml(Object obj, Class<?>... classes) throws ValidationException
   {
      int index = -1;
      for (int i = 0; i < classes.length; i++)
      {
         if (obj.getClass() == classes[i])
         {
            index = i;
            break;
         }
      }
      if (index == -1)
      {
         classes = Arrays.copyOf(classes, classes.length + 1);
         classes[classes.length - 1] = obj.getClass();
      }

      JAXBContext jc;
      try
      {
         jc = JAXBContext.newInstance(classes);
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e);
      }
      validateXml(obj, jc);
   }

   /**
    * Generates a schema for the JAXBContext 'jc' and validates the 
    * Object 'obj' against that schema.
    * @param obj
    * @param jc
    * @throws ValidationException
    */
   public static void validateXml(Object obj, JAXBContext jc) throws ValidationException
   {
      try
      {
         Marshaller m = jc.createMarshaller();
         final List<StringWriter> outs = new ArrayList<StringWriter>();
         jc.generateSchema(new SchemaOutputResolver()
         {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException
            {
               StringWriter out = new StringWriter();
               outs.add(out);
               StreamResult streamResult = new StreamResult(out);
               streamResult.setSystemId("");
               return streamResult;
            }
         });
         StreamSource[] sources = new StreamSource[outs.size()];
         int i = 0;
         for (StringWriter writer : outs)
         {
            writer.flush();
            String source = writer.toString();
            // System.out.println(source);
            sources[i++] = new StreamSource(new StringReader(source), "");
         }
         SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
         m.setSchema(sf.newSchema(sources));
         m.marshal(obj, new DefaultHandler());
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (SAXException e)
      {
         throw new RuntimeException(e);
      }
      catch (JAXBException e)
      {
         if (e instanceof ValidationException)
         {
            throw (ValidationException) e;
         }
         else
         {
            throw new RuntimeException(e);
         }
      }
   }

}
