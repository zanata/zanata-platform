package org.zanata.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.zanata.rest.dto.Account;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Links;
import org.zanata.rest.dto.Person;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.ProjectList;
import org.zanata.rest.dto.ProjectType;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;


public class GenerateSchema
{

   public static void main(String[] args) throws IOException, JAXBException
   {
      Class<?>[] classes = new Class<?>[] { Account.class, HeaderEntry.class, Link.class, Links.class, Person.class, PoHeader.class, PoTargetHeader.class, PotEntryHeader.class, Project.class, ProjectIteration.class, ProjectList.class, ProjectType.class, Resource.class, ResourceMeta.class, SimpleComment.class, TextFlow.class, TextFlowTarget.class, TranslationsResource.class };
      JAXBContext context = JAXBContext.newInstance(classes);

      generateSchemaToStdout(context);

   }

   public static void generateSchemaToStdout(JAXBContext context) throws IOException
   {
      final TreeMap<String, String> outputMap = new TreeMap<String, String>();
      SchemaOutputResolver schemaOutputResolver = new SchemaOutputResolver()
      {

         @Override
         public Result createOutput(final String namespaceUri, String suggestedFileName) throws IOException
         {
            StringWriter writer = new StringWriter()
            {
               @Override
               public void close() throws IOException
               {
                  super.close();
                  outputMap.put(namespaceUri, super.toString());
               }
            };
            StreamResult result = new StreamResult(writer);
            result.setSystemId("stdout");
            return result;
         }
      };

      context.generateSchema(schemaOutputResolver);
      // System.out.println(outputMap);
      for (String namespace : outputMap.keySet())
      {
         System.out.println("schema for namespace: '" + namespace + "'");
         System.out.println(outputMap.get(namespace));
      }
   }

}
