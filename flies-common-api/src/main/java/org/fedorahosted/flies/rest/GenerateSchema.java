package org.fedorahosted.flies.rest;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectList;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.resource.ResourceMeta;
import org.fedorahosted.flies.rest.dto.resource.TextFlow;
import org.fedorahosted.flies.rest.dto.resource.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.resource.Resource;
import org.fedorahosted.flies.rest.dto.resource.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.resource.TranslationsResource;

public class GenerateSchema
{

   public static void main(String[] args) throws IOException, JAXBException
   {
      Class<?>[] classes = new Class<?>[] { ProjectList.class, Project.class, ProjectIteration.class, Resource.class, ResourceMeta.class, TranslationsResource.class, TextFlow.class, TextFlowTarget.class };
      JAXBContext context = JAXBContext.newInstance(classes);
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
      // GenerateSamples.namespacePrefixMapper);

      SchemaOutputResolver schemaOutputResolver = new SchemaOutputResolver()
      {

         @Override
         public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException
         {
            StreamResult result = new StreamResult(new PrintStream(System.out)
            {
               @Override
               public void close()
               {
               }
            });
            result.setSystemId("stdout");
            return result;

         }
      };

      context.generateSchema(schemaOutputResolver);

   }

}
