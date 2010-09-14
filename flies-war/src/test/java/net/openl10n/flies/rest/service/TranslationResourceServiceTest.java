package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import javassist.compiler.NoFieldException;

import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.FliesRestTest;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.ResourceType;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.dao.LocaleDAO;
import net.openl10n.flies.dao.TextFlowTargetDAO;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.PoHeader;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;
import net.openl10n.flies.service.impl.LocaleServiceImpl;

import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TranslationResourceServiceTest extends FliesRestTest
{

   private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/1.0/r/";

   IMocksControl mockControl = EasyMock.createControl();
   Identity mockIdentity = mockControl.createMock(Identity.class);

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod
   void reset()
   {
      mockControl.reset();
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
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

      LocaleServiceImpl localeService = new LocaleServiceImpl();
      LocaleDAO LocaleDAO = new LocaleDAO(getSession());
      localeService.setLocaleDAO(LocaleDAO);
      TranslationResourcesService obj = new TranslationResourcesService(projectIterationDAO, documentDAO, personDAO, textFlowTargetDAO, localeService, resourceUtils, eTagUtils);

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

      // @formatter:off
      /*
      TODO: move this into an AbstractResourceMeta test (PoHeader is valid for source documents, not target)

      PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
      sr.getExtensions(true).add(poHeaderExt);
      
      */
      // @formatter:on

      ClientResponse<String> postResponse = client.post(sr, null); // new
                                                                   // StringSet(PoHeader.ID));
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      doGetandAssertThatResourceListContainsNItems(1);

      ClientResponse<Resource> resourceGetResponse = client.getResource("my.txt", null);// new
                                                                                        // StringSet(PoHeader.ID));
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));

      // @formatter:off
      /*
      TODO: move this into an AbstractResourceMeta test

      assertThat(gotSr.getExtensions().size(), is(1));
      PoHeader gotPoHeader = gotSr.getExtensions().findByType(PoHeader.class);
      assertThat(gotPoHeader, notNullValue());
      assertThat(poHeaderExt.getComment(), is(gotPoHeader.getComment()));
      assertThat(poHeaderExt.getEntries(), is(gotPoHeader.getEntries()));
      */
      // @formatter:on
   }

   // FIXME fix this broken test: it works in Eclipse but not Maven
   @Test(enabled = false)
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

      LocaleId de_DE = new LocaleId("de-DE");
      ClientResponse<String> response = client.putTranslations("my.txt", de_DE, entity);

      assertThat(response.getResponseStatus(), is(Status.OK));

      ClientResponse<TranslationsResource> getResponse = client.getTranslations("my.txt", de_DE);
      assertThat(getResponse.getResponseStatus(), is(Status.OK));
      TranslationsResource entity2 = getResponse.getEntity();
      assertThat(entity2.getTextFlowTargets(true).size(), is(entity.getTextFlowTargets(true).size()));

      entity.getTextFlowTargets(true).clear();
      response = client.putTranslations("my.txt", de_DE, entity);

      assertThat(response.getResponseStatus(), is(Status.OK));

      getResponse = client.getTranslations("my.txt", de_DE);
      // TODO this should return an empty set of targets, possibly with metadata
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