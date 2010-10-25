package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.rest.RestUtil;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.HeaderEntry;
import net.openl10n.flies.rest.dto.extensions.gettext.PoHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;
import net.openl10n.flies.service.impl.LocaleServiceImpl;

import org.apache.commons.httpclient.URIException;
import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fest.assertions.Assertions;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.delegates.UriHeaderDelegate;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TranslationResourceRestTest extends FliesRestTest
{
   private static final Logger log = LoggerFactory.getLogger(TranslationResourceRestTest.class);

   private static final String projectSlug = "sample-project";
   private static final String iter = "1.1";
   private static final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/1.0/r/";
   private static final String BAD_RESOURCE_PATH = "/projects/p/nonexistentProject/iterations/i/99.9/r/";
   private static final LocaleId DE = LocaleId.fromJavaName("de");
   private static final LocaleId FR = LocaleId.fromJavaName("fr");

   private static final String DOC2_NAME = "test.properties";
   private static final String DOC1_NAME = "foo.properties";

   StringSet extGettextComment = new StringSet("gettext;comment");
   StringSet extComment = new StringSet("comment");

   IMocksControl mockControl = EasyMock.createControl();
   Identity mockIdentity = mockControl.createMock(Identity.class);

   ProjectIterationDAO projectIterationDAO;
   DocumentDAO documentDAO;
   PersonDAO personDAO;
   TextFlowTargetDAO textFlowTargetDAO;
   ResourceUtils resourceUtils;
   ETagUtils eTagUtils;

   ITranslationResources transResource;

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
      this.projectIterationDAO = new ProjectIterationDAO(getSession());
      this.documentDAO = new DocumentDAO(getSession());
      this.personDAO = new PersonDAO(getSession());
      this.textFlowTargetDAO = new TextFlowTargetDAO(getSession());
      this.resourceUtils = new ResourceUtils();
      this.eTagUtils = new ETagUtils(getSession(), documentDAO);

      LocaleServiceImpl localeService = new LocaleServiceImpl();
      LocaleDAO localeDAO = new LocaleDAO(getSession());
      localeService.setLocaleDAO(localeDAO);
      TranslationResourcesService obj = new TranslationResourcesService(projectIterationDAO, documentDAO, personDAO, textFlowTargetDAO, localeService, resourceUtils, mockIdentity, eTagUtils);

      resources.add(obj);
   }

   @BeforeMethod(dependsOnMethods = "prepareRestEasyFramework")
   public void createClient()
   {
      this.transResource = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(RESOURCE_PATH));
   }

   @Test
   public void fetchEmptyListOfResources()
   {
      doGetandAssertThatResourceListContainsNItems(0);
   }

   @Test
   public void createEmptyResource()
   {
      Resource sr = createSourceResource("my.txt");

      ClientResponse<String> response = transResource.post(sr, null);
      assertThat(response.getResponseStatus(), is(Status.CREATED));
      List<String> locationHeader = response.getHeaders().get("Location");
      assertThat(locationHeader.size(), is(1));
      assertThat(locationHeader.get(0), endsWith("r/my.txt"));
      doGetandAssertThatResourceListContainsNItems(1);
   }

   @Test
   public void createResourceWithContentUsingPost()
   {
      Resource sr = createSourceResource("my.txt");

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      ClientResponse<String> postResponse = transResource.post(sr, null);
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      postResponse = transResource.post(sr, null);

      ClientResponse<Resource> resourceGetResponse = transResource.getResource("my.txt", null);
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));

   }

   @Test
   public void createResourceWithContentUsingPut()
   {
      Resource sr = createSourceResource("my.txt");

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      ClientResponse<String> response = transResource.putResource("my.txt", sr, null);
      assertThat(response.getResponseStatus(), is(Status.CREATED));
      assertThat(response.getLocation().getHref(), endsWith("/r/my.txt"));

      ClientResponse<Resource> resourceGetResponse = transResource.getResource("my.txt", null);
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));
      Resource gotSr = resourceGetResponse.getEntity();
      assertThat(gotSr.getTextFlows().size(), is(1));
      assertThat(gotSr.getTextFlows().get(0).getContent(), is("tf1"));

   }

   @Test
   public void createPoResourceWithPoHeader()
   {
      String docName = "my.txt";
      String docUri = RestUtil.convertToDocumentURIId(docName);
      Resource sr = createSourceResource(docName);

      TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
      sr.getTextFlows().add(stf);

      // @formatter:off
      /*
      TODO: move this into an AbstractResourceMeta test (PoHeader is valid for source documents, not target)

      PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
      sr.getExtensions(true).add(poHeaderExt);
      
      */
      // @formatter:on

      ClientResponse<String> postResponse = transResource.post(sr, null); // new
                                                                   // StringSet(PoHeader.ID));
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      doGetandAssertThatResourceListContainsNItems(1);

      ClientResponse<Resource> resourceGetResponse = transResource.getResource(docUri, null);// new
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

   // NB this test breaks in Maven if the dev profile is active (because of the
   // imported testdata)
   @Test
   public void publishTranslations()
   {
      createResourceWithContentUsingPut();

      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget();
      target.setResId("tf1");
      target.setContent("hello world");
      target.setState(ContentState.Approved);
      target.setTranslator(new Person("root@localhost", "Admin user"));
      entity.getTextFlowTargets().add(target);

      LocaleId de_DE = new LocaleId("de");
      ClientResponse<String> response = transResource.putTranslations("my.txt", de_DE, entity, null);

      assertThat(response.getResponseStatus(), is(Status.OK));

      ClientResponse<TranslationsResource> getResponse = transResource.getTranslations("my.txt", de_DE, null);
      assertThat(getResponse.getResponseStatus(), is(Status.OK));
      TranslationsResource entity2 = getResponse.getEntity();
      assertThat(entity2.getTextFlowTargets().size(), is(entity.getTextFlowTargets().size()));

      entity.getTextFlowTargets().clear();
      response = transResource.putTranslations("my.txt", de_DE, entity, null);

      assertThat(response.getResponseStatus(), is(Status.OK));

      getResponse = transResource.getTranslations("my.txt", de_DE, null);
      assertThat(getResponse.getResponseStatus(), is(Status.NOT_FOUND));
   }

   @Test
   public void getDocumentThatDoesntExist()
   {
      ClientResponse<Resource> clientResponse = transResource.getResource("my,doc,does,not,exist.txt", null);
      assertThat(clientResponse.getResponseStatus(), is(Status.NOT_FOUND));
   }

   @Test
   public void getDocument() throws URIException
   {
      String docName = "my/path/document.txt";
      String docUri = RestUtil.convertToDocumentURIId(docName);
      Resource resource = createSourceDoc(docName, false);
      transResource.putResource(docUri, resource, null);

      ClientResponse<ResourceMeta> response = transResource.getResourceMeta(docUri, null);
      assertThat(response.getResponseStatus(), is(Status.OK));
      ResourceMeta doc = response.getEntity();
      assertThat(doc.getName(), is(docName));
      assertThat(doc.getContentType(), is(ContentType.TextPlain));
      assertThat(doc.getLang(), is(LocaleId.EN_US));
      assertThat(doc.getRevision(), is(1));

      /*
       * Link link = doc.getLinks().findLinkByRel(Relationships.SELF);
       * assertThat( link, notNullValue() ); assertThat(
       * URIUtil.decode(link.getHref().toString()), endsWith(url+docUri) );
       * 
       * link = doc.getLinks().findLinkByRel(Relationships.DOCUMENT_CONTAINER);
       * assertThat( link, notNullValue() ); assertThat(
       * link.getHref().toString(), endsWith("iterations/i/1.0") );
       */
   }

   @Test
   public void getDocumentWithResources() throws URIException
   {
      LocaleId nbLocale = new LocaleId("de");
      String docName = "my/path/document.txt";
      String docUri = RestUtil.convertToDocumentURIId(docName);
      Resource resource = createSourceDoc(docName, true);
      transResource.putResource(docUri, resource, null);
      TranslationsResource trans = createTargetDoc();
      transResource.putTranslations(docUri, nbLocale, trans, null);

      {
         ClientResponse<Resource> response = transResource.getResource(docUri, null);
         assertThat(response.getResponseStatus(), is(Status.OK));

         Resource doc = response.getEntity();
         assertThat(doc.getTextFlows().size(), is(1));
      }

      ClientResponse<TranslationsResource> response = transResource.getTranslations(docUri, nbLocale, null);
      assertThat(response.getResponseStatus(), is(Status.OK));

      TranslationsResource doc = response.getEntity();
      assertThat("should have one textFlow", doc.getTextFlowTargets().size(), is(1));
      TextFlowTarget tft = doc.getTextFlowTargets().get(0);

      assertThat(tft, notNullValue());
      assertThat("should have a textflow with this id", tft.getResId(), is("tf1"));

      assertThat("expected de target", tft, notNullValue());
      assertThat("expected translation for de", tft.getContent(), is("hei verden"));
   }

   @Test
   public void putNewDocument()
   {
      String docName = "my/fancy/document.txt";
      String docUrl = RestUtil.convertToDocumentURIId(docName);
      Resource doc = createSourceDoc(docName, false);
      Response response = transResource.putResource(docUrl, doc, null);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
      assertThat(response.getMetadata().getFirst("Location").toString(), endsWith(RESOURCE_PATH + docUrl));

      ClientResponse<Resource> documentResponse = transResource.getResource(docUrl, null);

      assertThat(documentResponse.getResponseStatus(), is(Status.OK));

      doc = documentResponse.getEntity();
      assertThat(doc.getRevision(), is(1));

      /*
       * Link link = doc.getLinks().findLinkByRel(Relationships.SELF);
       * assertThat(link, notNullValue()); assertThat(link.getHref().toString(),
       * endsWith(url + docUrl));
       * 
       * link = doc.getLinks().findLinkByRel(Relationships.DOCUMENT_CONTAINER);
       * assertThat(link, notNullValue()); assertThat(link.getType(),
       * is(MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML));
       */
   }

   @Test
   public void putNewDocumentWithResources() throws Exception
   {
      String docName = "my/fancy/document.txt";
      String docUrl = RestUtil.convertToDocumentURIId(docName);
      Resource doc = createSourceDoc(docName, false);

      List<TextFlow> textFlows = doc.getTextFlows();
      textFlows.clear();

      TextFlow textFlow = new TextFlow("tf1");
      textFlow.setContent("hello world!");
      textFlows.add(textFlow);

      TextFlow tf3 = new TextFlow("tf3");
      tf3.setContent("more text");
      textFlows.add(tf3);

      Marshaller m = null;
      JAXBContext jc = JAXBContext.newInstance(Resource.class);
      m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      m.marshal(doc, System.out);

      Response response = transResource.putResource(docUrl, doc, null);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
      assertThat(response.getMetadata().getFirst("Location").toString(), endsWith(RESOURCE_PATH + docUrl));

      ClientResponse<Resource> documentResponse = transResource.getResource(docUrl, null);

      assertThat(documentResponse.getResponseStatus(), is(Status.OK));

      doc = documentResponse.getEntity();

      assertThat(doc.getRevision(), is(1));

      assertThat("Should have textFlows", doc.getTextFlows(), notNullValue());
      assertThat("Should have 2 textFlows", doc.getTextFlows().size(), is(2));
      assertThat("Should have tf1 textFlow", doc.getTextFlows().get(0).getId(), is("tf1"));
      assertThat("Container1 should have tf3 textFlow", doc.getTextFlows().get(1).getId(), is(tf3.getId()));

      textFlow = doc.getTextFlows().get(0);
      textFlow.setId("tf2");

      response = transResource.putResource(docUrl, doc, null);

      // this WAS testing for status 205
      assertThat(response.getStatus(), is(200));

      documentResponse = transResource.getResource(docUrl, null);
      assertThat(documentResponse.getResponseStatus(), is(Status.OK));
      doc = documentResponse.getEntity();

      assertThat(doc.getRevision(), is(2));

      assertThat("Should have textFlows", doc.getTextFlows(), notNullValue());
      assertThat("Should have two textFlows", doc.getTextFlows().size(), is(2));
      assertThat("should have same id", doc.getTextFlows().get(0).getId(), is("tf2"));
   }

   @Test
   public void getZero() throws Exception
   {
      expectDocs(true, false);
   }

   @Test
   public void put1Get() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
   }

   @Test
   public void put1Post2Get() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      Resource doc2 = postDoc2(false);
      doc2.setRevision(1);
      TextFlow tf2 = doc2.getTextFlows().get(0);
      tf2.setRevision(1);
      TranslationsResource target2 = putTarget2();
      TextFlowTarget tft2 = target2.getTextFlowTargets().get(0);
      tft2.setTextFlowRevision(1);
      expectDocs(true, false, doc1, doc2);
      expectTarget1(target1);
      expectTarget2(target2);
   }

   @Test
   public void put1Post2Put1() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      Resource doc2 = postDoc2(false);
      doc2.setRevision(1);
      TextFlow tf2 = doc2.getTextFlows().get(0);
      tf2.setRevision(1);
      TranslationsResource target2 = putTarget2();
      TextFlowTarget tft2 = target2.getTextFlowTargets().get(0);
      tft2.setTextFlowRevision(1);
      expectDocs(true, false, doc1, doc2);
      expectTarget1(target1);
      expectTarget2(target2);
      // this put should have the effect of deleting doc2
      putDoc1(false);
      deleteDoc2();
      // should be identical to doc1 from before, including revisions
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      dontExpectTarget2();
      // expectTargets(true, FR, target2);
      // use dao to check that doc2 is marked obsolete
      verifyObsoleteDocument(doc2.getName());
   }

   @Test
   public void put1Delete1Put1() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      deleteDoc1(); // doc1 becomes obsolete
      getZero();
      dontExpectTarget1();
      putDoc1(false); // doc1 resurrected, rev 1
      doc1.setRevision(1);
      tf1.setRevision(1);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
   }

   @Test
   public void put1Put1Again() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      putDoc1(false); // docRev still 1
      doc1.setRevision(1);
      tf1.setRevision(1);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
   }

   public void put1Put1WithAnotherTextFlow()
   {
      // TODO make sure tft1 is still there even though the doc rev goes up
   }

   @Test
   public void put1Delete1Put1a() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      deleteDoc1(); // doc1 becomes obsolete
      getZero();
      dontExpectTarget1();
      Resource doc1a = putDoc1a(false); // doc1 resurrected, rev 2
      doc1a.setRevision(2);
      TextFlow tf1a = doc1a.getTextFlows().get(0);
      tf1a.setRevision(doc1a.getRevision());
      TranslationsResource target1a = putTarget1a();
      TextFlowTarget tft1a = target1a.getTextFlowTargets().get(0);
      tft1a.setTextFlowRevision(tf1a.getRevision());
      expectDocs(true, false, doc1a);
      dontExpectTarget1();
      expectTarget1a(target1a);
   }

   @Test
   public void putPoPotGet() throws Exception
   {
      getZero();
      Resource po1 = putPo1();
      expectDocs(false, false, po1);
      TranslationsResource poTarget1 = putPoTarget1();
      expectTarget(false, po1.getName(), DE, poTarget1);
   }

   @Test
   public void put1Put1aPut1() throws Exception
   {
      getZero();
      Resource doc1 = putDoc1(false);
      doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
      tft1.setTextFlowRevision(1);
      expectDocs(true, false, doc1);
      expectTarget1(target1);
      // this should completely replace doc1's textflow FOOD with HELLO
      Resource doc1a = putDoc1a(false);
      doc1a.setRevision(2);
      TextFlow tf1a = doc1a.getTextFlows().get(0);
      tf1a.setRevision(2);
      TranslationsResource target1a = putTarget1a();
      TextFlowTarget tft1a = target1a.getTextFlowTargets().get(0);
      tft1a.setTextFlowRevision(2);
      expectDocs(true, false, doc1a);
      dontExpectTarget1();
      expectTarget1a(target1a);
      // use dao to check that the HTextFlow FOOD (from doc1) is marked obsolete
      verifyObsoleteResource(doc1.getName(), "FOOD");
      putDoc1(false); // same as original doc1, but different doc rev
      doc1.setRevision(3);
      expectDocs(true, false, doc1);
      // target 1 should be resurrected
      expectTarget1(target1);
      dontExpectTarget1a();
   }

   @Test
   public void getBadProject() throws Exception
   {
      ITranslationResources badTransResource = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(BAD_RESOURCE_PATH));
      ClientResponse<List<ResourceMeta>> response = badTransResource.get(null);
      assertThat(response.getStatus(), is(404));
   }

   // END of tests

   private void expectDocs(boolean checkRevs, boolean checkLinksIgnored, Resource... docs)
   {
      expectResourceMetas(checkRevs, docs);
      expectResources(checkRevs, docs);
   }

   private void expectResourceMetas(boolean checkRevs, AbstractResourceMeta... docs)
   {
      ClientResponse<List<ResourceMeta>> response = transResource.get(null);

      assertThat(response.getStatus(), is(200));
      List<ResourceMeta> actualDocs = response.getEntity();
      assertThat(actualDocs, notNullValue());
      Map<String, AbstractResourceMeta> expectedDocs = new HashMap<String, AbstractResourceMeta>();
      for (AbstractResourceMeta doc : docs)
      {
         expectedDocs.put(doc.getName(), doc);
      }
      // Set<String> actualDocVals = new TreeSet<String>();
      Map<String, AbstractResourceMeta> actualDocsMap = new HashMap<String, AbstractResourceMeta>();
      for (ResourceMeta doc : actualDocs)
      {
         actualDocsMap.put(doc.getName(), doc);
         log.debug("actual doc: " + doc.toString());
         AbstractResourceMeta expectedDoc = expectedDocs.get(doc.getName());
         if (checkRevs)
            assertThat(doc.getRevision(), is(expectedDoc.getRevision()));
      }
      assertThat(actualDocsMap.keySet(), is(expectedDocs.keySet()));
   }

   private void expectResources(boolean checkRevs, Resource... docs)
   {
      for (Resource expectedDoc : docs)
      {
         if (!checkRevs)
            ResourceTestUtil.clearRevs(expectedDoc);
         ClientResponse<Resource> response = transResource.getResource(expectedDoc.getName(), extGettextComment);
         assertThat(response.getStatus(), is(200));
         Resource actualDoc = response.getEntity();
         if (!checkRevs)
            ResourceTestUtil.clearRevs(actualDoc);
         createExtensionSets(expectedDoc);
         createExtensionSets(actualDoc);
         Assertions.assertThat(actualDoc).isEqualTo(expectedDoc);
      }
   }

   protected void createExtensionSets(Resource resource)
   {
      resource.getExtensions(true);
      for (TextFlow tf : resource.getTextFlows())
      {
         tf.getExtensions(true);
      }
   }

   private void dontExpectTarget(String id, LocaleId locale)
   {
      ClientResponse<TranslationsResource> response = transResource.getTranslations(id, locale, null);
      assertThat(response.getStatus(), is(404));
   }

   private void expectTarget(boolean checkRevs, String id, LocaleId locale, TranslationsResource expectedDoc)
   {
      ClientResponse<TranslationsResource> response = transResource.getTranslations(id, locale, extGettextComment);
      assertThat(response.getStatus(), is(200));
      TranslationsResource actualDoc = response.getEntity();
      actualDoc.getLinks(true).clear();
      actualDoc.getExtensions(true);

      for (TextFlowTarget tft : expectedDoc.getTextFlowTargets())
      {
         tft.getExtensions(true);
      }

      for (TextFlowTarget tft : actualDoc.getTextFlowTargets())
      {
         tft.getExtensions(true);
      }

      expectedDoc.getLinks(true).clear();
      expectedDoc.getExtensions(true);
      if (!checkRevs)
      {
         ResourceTestUtil.clearRevs(actualDoc);
         ResourceTestUtil.clearRevs(expectedDoc);
      }
      Assertions.assertThat(actualDoc.toString()).isEqualTo(expectedDoc.toString());
   }

   private Resource createSourceDoc(String name, boolean withTextFlow)
   {
      Resource resource = new Resource();
      resource.setContentType(ContentType.TextPlain);
      resource.setLang(LocaleId.EN_US);
      resource.setName(name);
      resource.setType(ResourceType.DOCUMENT);

      if (withTextFlow)
         resource.getTextFlows().add(new TextFlow("tf1", LocaleId.EN_US, "hello world"));
      return resource;
   }

   private TranslationsResource createTargetDoc()
   {
      TranslationsResource trans = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget();
      target.setContent("hei verden");
      target.setDescription("translation of hello world");
      target.setResId("tf1");
      target.setState(ContentState.Approved);
      Person person = new Person("email@example.com", "Translator Name");
      target.setTranslator(person);
      trans.getTextFlowTargets().add(target);
      return trans;
   }

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
      ClientResponse<List<ResourceMeta>> resources = transResource.get(null);
      assertThat(resources.getResponseStatus(), is(Status.OK));

      assertThat(resources.getEntity().size(), is(n));
   }

   private Resource newDoc(String id, TextFlow... textFlows)
   {
      Resource doc = new Resource(id);
      doc.setLang(LocaleId.EN);
      doc.setContentType(ContentType.TextPlain);
      doc.setType(ResourceType.FILE);
      doc.setRevision(null);
      for (TextFlow textFlow : textFlows)
      {
         doc.getTextFlows().add(textFlow);
      }
      return doc;
   }

   private TextFlow newTextFlow(String id, String sourceContent, String sourceComment)
   {
      TextFlow textFlow = new TextFlow(id, LocaleId.EN);
      textFlow.setContent(sourceContent);
      if (sourceComment != null)
         getOrAddComment(textFlow).setValue(sourceComment);
      return textFlow;
   }

   private TextFlowTarget newTextFlowTarget(String id, String targetContent, String targetComment)
   {
      TextFlowTarget target = new TextFlowTarget();
      target.setResId(id);
      target.setState(ContentState.Approved);
      target.setContent(targetContent);
      if (targetComment != null)
         getOrAddComment(target).setValue(targetComment);
      return target;
   }

   SimpleComment getOrAddComment(TextFlow tf)
   {
      return tf.getExtensions(true).findOrAddByType(SimpleComment.class);
   }

   SimpleComment getOrAddComment(TextFlowTarget tft)
   {
      return tft.getExtensions(true).findOrAddByType(SimpleComment.class);
   }

   private Resource putPo1()
   {
      String id = "foo.pot";
      TextFlow textflow = newTextFlow("FOOD", "Slime Mould", "POT comment");
      PotEntryHeader poData = textflow.getExtensions(true).findOrAddByType(PotEntryHeader.class);
      poData.setContext("context");
      poData.setExtractedComment("Tag: title");
      List<String> flags = poData.getFlags();
      flags.add("no-c-format");
      flags.add("flag2");
      List<String> refs = poData.getReferences();
      refs.add("ref1.xml:7");
      refs.add("ref1.xml:21");

      Resource doc = newDoc(id, textflow);
      PoHeader poHeader = doc.getExtensions(true).findOrAddByType(PoHeader.class);
      poHeader.setComment("poheader comment");
      List<HeaderEntry> poEntries = poHeader.getEntries();
      poEntries.add(new HeaderEntry("Project-Id-Version", "en"));

      log.debug("{}", doc);
      Response response = transResource.putResource(id, doc, extGettextComment);
      assertThat(response.getStatus(), isOneOf(200, 201));
      return doc;
   }

   private TranslationsResource putPoTarget1()
   {
      String id = "foo.pot";
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("FOOD", "Sauerkraut", "translator comment");
      tr.getTextFlowTargets().add(target);

      PoTargetHeader targetHeader = tr.getExtensions(true).findOrAddByType(PoTargetHeader.class);
      targetHeader.setComment("target comment");
      List<HeaderEntry> entries = targetHeader.getEntries();
      entries.add(new HeaderEntry("Project-Id-Version", "ja"));

      transResource.putTranslations(id, DE, tr, extGettextComment);

      return tr;
   }

   private Resource putDoc1(boolean putTarget)
   {
      String id = DOC1_NAME;
      Resource doc = newDoc(id, newTextFlow("FOOD", "Slime Mould", "slime mould comment"));
      Response response = transResource.putResource(id, doc, extComment);
      assertThat(response.getStatus(), isOneOf(200, 201));

      if (putTarget)
         putTarget1();

      return doc;
   }

   protected TranslationsResource putTarget1()
   {
      String id = DOC1_NAME;
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("FOOD", "Sauerkraut", null);
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, DE, tr, extGettextComment);
      return tr;
   }

   private void dontExpectTarget1()
   {
      String id = DOC1_NAME;
      dontExpectTarget(id, DE);
   }

   private void expectTarget1(TranslationsResource target1)
   {
      String id = DOC1_NAME;
      expectTarget(true, id, DE, target1);
   }

   private Resource putDoc1a(boolean putTarget)
   {
      String id = DOC1_NAME;
      Resource doc = newDoc(id, newTextFlow("HELLO", "Hello World", null));
      Response response = transResource.putResource(id, doc, extComment);
      assertThat(response.getStatus(), isOneOf(200, 201));

      if (putTarget)
         putTarget1a();

      return doc;
   }

   protected TranslationsResource putTarget1a()
   {
      String id = DOC1_NAME;
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("HELLO", "Bonjour le Monde", "bon jour comment");
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, FR, tr, extGettextComment);
      return tr;
   }

   private void dontExpectTarget1a()
   {
      String id = DOC1_NAME;
      dontExpectTarget(id, FR);
   }

   private void expectTarget1a(TranslationsResource target1a)
   {
      String id = DOC1_NAME;
      expectTarget(true, id, FR, target1a);
   }

   private void deleteDoc1()
   {
      deleteDoc(DOC1_NAME);
   }

   private void deleteDoc2()
   {
      deleteDoc(DOC2_NAME);
   }

   protected void deleteDoc(String id)
   {
      Response response = transResource.deleteResource(id);
      assertThat(response.getStatus(), is(200));
   }

   private Resource postDoc2(boolean putTarget)
   {
      String id = DOC2_NAME;
      Resource doc = newDoc(id, newTextFlow("HELLO", "Hello World", "hello comment"));
      Response response = transResource.post(doc, extComment);
      assertThat(response.getStatus(), is(201));

      if (putTarget)
         putTarget2();

      return doc;
   }

   protected TranslationsResource putTarget2()
   {
      String id = DOC2_NAME;
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("HELLO", "Bonjour le Monde", null);
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, FR, tr, extGettextComment);
      return tr;
   }

   private void dontExpectTarget2()
   {
      String id = DOC2_NAME;
      dontExpectTarget(id, FR);
   }

   private void expectTarget2(TranslationsResource target2)
   {
      String id = DOC2_NAME;
      expectTarget(true, id, FR, target2);
   }

   private void verifyObsoleteDocument(final String docID) throws Exception
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iter);
      Map<String, HDocument> allDocuments = iteration.getAllDocuments();
      HDocument hDocument = allDocuments.get(docID);
      // FIXME hDocument is coming back null
      // Assert.assertNotNull(hDocument);
      // Assert.assertTrue(hDocument.isObsolete());
   }

   private void verifyObsoleteResource(final String docID, final String resourceID) throws Exception
   {
      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iter);
      Map<String, HDocument> allDocuments = iteration.getAllDocuments();
      HDocument hDocument = allDocuments.get(docID);
      // FIXME hDocument is coming back null
      // HTextFlow hResource = hDocument.getAllTextFlows().get(resourceID);
      // Assert.assertNotNull(hResource);
      // Assert.assertTrue(hResource.isObsolete());
   }
}