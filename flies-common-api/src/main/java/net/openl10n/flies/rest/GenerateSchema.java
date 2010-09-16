package net.openl10n.flies.rest;

import java.io.IOException;
import java.io.PrintStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import net.openl10n.flies.rest.dto.Account;
import net.openl10n.flies.rest.dto.Link;
import net.openl10n.flies.rest.dto.Links;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.Project;
import net.openl10n.flies.rest.dto.ProjectIteration;
import net.openl10n.flies.rest.dto.ProjectList;
import net.openl10n.flies.rest.dto.ProjectType;
import net.openl10n.flies.rest.dto.extensions.PoHeader;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.PotEntryHeader;
import net.openl10n.flies.rest.dto.extensions.SimpleComment;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

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
