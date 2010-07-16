package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.FliesRestTest;
import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.dao.DocumentDAO;
import org.fedorahosted.flies.dao.PersonDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.rest.LanguageQualifier;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.Person;
import org.fedorahosted.flies.rest.dto.extensions.PoHeader;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.flies.rest.dto.resource.AbstractResourceMeta;
import org.fedorahosted.flies.rest.dto.resource.ResourceMeta;
import org.fedorahosted.flies.rest.dto.resource.ResourceMetaList;
import org.fedorahosted.flies.rest.dto.resource.Resource;
import org.fedorahosted.flies.rest.dto.resource.TextFlow;
import org.fedorahosted.flies.rest.dto.resource.TextFlow;
import org.fedorahosted.flies.rest.dto.resource.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.resource.TranslationsResource;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.Test;

public class TranslationResourceServiceTest extends FliesRestTest
{

   private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/1.0/resources";

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Override
   protected void prepareResources()
   {
      final ProjectIterationDAO projectIterationDAO = new ProjectIterationDAO(getSession());
      final DocumentDAO documentDAO = new DocumentDAO(getSession());
      final PersonDAO personDAO = new PersonDAO(getSession());
      final TextFlowTargetDAO textFlowTargetDAO = new TextFlowTargetDAO(getSession());
      final ResourceUtils resourceUtils = new ResourceUtils();
      final ETagUtils eTagUtils = new ETagUtils(getSession(), documentDAO);

      TranslationResourcesService obj = new TranslationResourcesService(projectIterationDAO, documentDAO, personDAO, textFlowTargetDAO, resourceUtils, eTagUtils);

      resources.add(obj);
   }

   @Test
   public void fetchEmptyListOfResources()
   {
      doGetandAssertThatResourceListContainsNItems(0);
   }

   @Test
   public void createEmptyResource()
   {
      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

      Resource sr = createSourceResource("my.txt");
      ClientResponse<String> response = client.post(sr, null);
      assertThat(response.getResponseStatus(), is(Status.CREATED));
      List<String> locationHeader = response.getHeaders().get("Location");
      assertThat(locationHeader.size(), is(1));
      assertThat(locationHeader.get(0), endsWith("r/my.txt"));
      doGetandAssertThatResourceListContainsNItems(1);
   }

   @Test
   public void createResourceWithContentUsingPost()
   {
      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

      Resource sr = createSourceResource("my.txt");

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      ClientResponse<String> postResponse = client.post(sr, null);
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      postResponse = client.post(sr, null);

      ClientResponse<Resource> resourceGetResponse = client.getResource("my.txt", null);
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));

   }

   @Test
   public void createResourceWithContentUsingPut()
   {
      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

      Resource sr = createSourceResource("my.txt");

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      ClientResponse<String> response = client.putResource("my.txt", sr);
      assertThat(response.getResponseStatus(), is(Status.CREATED));
      assertThat(response.getLocation().getHref(), endsWith("/r/my.txt"));

      ClientResponse<Resource> resourceGetResponse = client.getResource("my.txt", null);
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));

   }

   @Test
   public void createPoResourceWithPoHeader()
   {
      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

      Resource sr = createSourceResource("my.txt");

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
      sr.getExtensions(true).add(poHeaderExt);

      ClientResponse<String> postResponse = client.post(sr, new StringSet(PoHeader.ID));
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      doGetandAssertThatResourceListContainsNItems(1);

      ClientResponse<Resource> resourceGetResponse = client.getResource("my.txt", new StringSet(PoHeader.ID));
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));
      assertThat(gotSr.getExtensions().size(), is(1));
      PoHeader gotPoHeader = gotSr.getExtensions().findByType(PoHeader.class);
      assertThat(gotPoHeader, notNullValue());
      assertThat(poHeaderExt.getComment(), is(gotPoHeader.getComment()));
      assertThat(poHeaderExt.getEntries(), is(gotPoHeader.getEntries()));

   }

   @Test
   public void publishTranslations()
   {
      createResourceWithContentUsingPut();

      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));

      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget("tf1");
      target.setContent("hello world");
      target.setState(ContentState.Approved);
      target.setTranslator(new Person("root@localhost", "Admin user"));
      entity.getTextFlowTargets(true).add(target);

      ClientResponse<String> response = client.putTranslations("my.txt", LocaleId.DE, entity);

      assertThat(response.getResponseStatus(), is(Status.OK));

      ClientResponse<TranslationsResource> getResponse = client.getTranslations("my.txt", LocaleId.DE);
      assertThat(getResponse.getResponseStatus(), is(Status.OK));
      TranslationsResource entity2 = getResponse.getEntity();
      assertThat(entity2.getTextFlowTargets(true).size(), is(entity.getTextFlowTargets(true).size()));

      entity.getTextFlowTargets(true).clear();
      response = client.putTranslations("my.txt", LocaleId.DE, entity);

      assertThat(response.getResponseStatus(), is(Status.OK));

      getResponse = client.getTranslations("my.txt", LocaleId.DE);
      assertThat(getResponse.getResponseStatus(), is(Status.NOT_FOUND));

   }

   // END of tests

   private Resource createSourceResource(String name)
   {
      Resource sr = new Resource(name);
      sr.setContentType(ContentType.TextPlain);
      sr.setLang(LocaleId.EN);
      sr.setType(ResourceType.FILE);
      return sr;
   }

   private void doGetandAssertThatResourceListContainsNItems(int n)
   {
      ITranslationResources client = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
      ClientResponse<List<ResourceMeta>> resources = client.get();
      assertThat(resources.getResponseStatus(), is(Status.OK));

      assertThat(resources.getEntity().size(), is(n));
   }

}