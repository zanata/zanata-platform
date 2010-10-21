package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.HeaderEntry;
import net.openl10n.flies.rest.dto.extensions.gettext.PoHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader;
import net.openl10n.flies.rest.dto.po.PotEntryData;
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
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TranslationResourceRestTest extends FliesRestTest
{
   private static final Logger log = LoggerFactory.getLogger(TranslationResourceRestTest.class);

   String projectSlug = "sample-project";
   String iter = "1.1";
   private final String RESOURCE_PATH = "/projects/p/sample-project/iterations/i/1.0/r/";
   private final String BAD_RESOURCE_PATH = "/projects/p/nonexistentProject/iterations/i/99.9/r/";
   private static final LocaleId DE = LocaleId.fromJavaName("de");
   private static final LocaleId FR = LocaleId.fromJavaName("fr");

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

      ClientResponse<String> postResponse = transResource.post(sr, null); // new
                                                                   // StringSet(PoHeader.ID));
      assertThat(postResponse.getResponseStatus(), is(Status.CREATED));
      doGetandAssertThatResourceListContainsNItems(1);

      ClientResponse<Resource> resourceGetResponse = transResource.getResource("my.txt", null);// new
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
   public void publishTranslations()
   {
      createResourceWithContentUsingPut();

      TranslationsResource entity = new TranslationsResource();
      TextFlowTarget target = new TextFlowTarget("tf1");
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
      // TODO IMHO this should return an empty set of targets, possibly with
      // metadata
      assertThat(getResponse.getResponseStatus(), is(Status.NOT_FOUND));
   }

   public void getDocumentThatDoesntExist()
   {
      ClientResponse<Resource> clientResponse = transResource.getResource("my,doc,does,not,exist.txt", null);
      assertThat(clientResponse.getResponseStatus(), is(Status.NOT_FOUND));
   }

   public void getDocument() throws URIException
   {
      // NB the new rest API does not map '/' to ','
      // if a document is PUT with a '/' in the docId, there is no
      // way to GET it back.
      String docUri = "my,path,document.txt";
      Resource resource = createSourceDoc(docUri, false);
      transResource.putResource(docUri, resource, null);

      ClientResponse<ResourceMeta> response = transResource.getResourceMeta(docUri, null);
      assertThat(response.getResponseStatus(), is(Status.OK));
      ResourceMeta doc = response.getEntity();
      assertThat(doc.getName(), is(docUri));
      assertThat(doc.getContentType(), is(ContentType.TextPlain));
      assertThat(doc.getLang(), is(LocaleId.EN_US));
      // FIXME check revision!
      // assertThat( doc.getRevision(), is(1) );

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

   public void getDocumentWithResources() throws URIException
   {
      LocaleId nbLocale = new LocaleId("de");
      String docUri = "my,path,document.txt";
      Resource resource = createSourceDoc(docUri, true);
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

   public void putNewDocument()
   {
      // NB the new rest API does not map '/' to ','
      String docUrl = "my,fancy,document.txt";
      Resource doc = createSourceDoc(docUrl, false);
      Response response = transResource.putResource(docUrl, doc, null);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
      assertThat(response.getMetadata().getFirst("Location").toString(), endsWith(RESOURCE_PATH + docUrl));

      ClientResponse<Resource> documentResponse = transResource.getResource(docUrl, null);

      assertThat(documentResponse.getResponseStatus(), is(Status.OK));

      doc = documentResponse.getEntity();
      // FIXME check revision!
      // assertThat(doc.getRevision(), is(1));

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

   public void putNewDocumentWithResources() throws Exception
   {
      // NB the new rest API does not map '/' to ','
      String docUrl = "my,fancy,document.txt";
      Resource doc = createSourceDoc(docUrl, false);

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

      // FIXME check revision!
      // assertThat(doc.getRevision(), is(1));

      assertThat("Should have textFlows", doc.getTextFlows(), notNullValue());
      assertThat("Should have 2 textFlows", doc.getTextFlows().size(), is(2));
      assertThat("Should have tf1 textFlow", doc.getTextFlows().get(0).getId(), is("tf1"));
      assertThat("Container1 should have tf3 textFlow", doc.getTextFlows().get(1).getId(), is(tf3.getId()));

      textFlow = doc.getTextFlows().get(0);
      textFlow.setId("tf2");

      response = transResource.putResource(docUrl, doc, null);

      // TODO this WAS testing for status 205
      assertThat(response.getStatus(), is(200));

      documentResponse = transResource.getResource(docUrl, null);
      assertThat(documentResponse.getResponseStatus(), is(Status.OK));
      doc = documentResponse.getEntity();

      // FIXME check revision!
      // assertThat(doc.getRevision(), is(2));

      assertThat("Should have textFlows", doc.getTextFlows(), notNullValue());
      assertThat("Should have two textFlows", doc.getTextFlows().size(), is(2));
      assertThat("should have same id", doc.getTextFlows().get(0).getId(), is("tf2"));
   }

   public void getZero() throws Exception
   {
      // log.info("getZero()");
      expectResourceMetas(true, new AbstractResourceMeta[0]);
   }

   @Test
   public void put1Get() throws Exception
   {
      log.info("putGet()");
      getZero();
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
   }

   public void put1Post2Get() throws Exception
   {
      log.info("putPostGet()");
      getZero();
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      expectDocs(true, false, doc1);
      Resource doc2 = postDoc2(false);
      // doc2.setRevision(1);
      TextFlow tf2 = doc2.getTextFlows().get(0);
      // tf2.setRevision(1);
      TranslationsResource target2 = putTarget2();
      // TextFlowTarget tft2 = getTextFlowTargets().get(0);
      // tft2.setResourceRevision(1);
      expectDocs(true, false, doc1, doc2);
      expectTargets(true, DE, target1);
      expectTargets(true, FR, target2);
   }

   public void put1Post2Put1() throws Exception
   {
      log.info("put2Then1()");
      getZero();
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      Resource doc2 = postDoc2(false);
      // doc2.setRevision(1);
      TextFlow tf2 = doc2.getTextFlows().get(0);
      // tf2.setRevision(1);
      TranslationsResource target2 = putTarget2();
      // TextFlowTarget tft2 = getTextFlowTargets().get(0);
      // tft2.setResourceRevision(1);
      expectDocs(true, false, doc1, doc2);
      expectTargets(true, DE, target1);
      expectTargets(true, FR, target2);
      // this put should have the effect of deleting doc2
      putDoc1(false);
      deleteDoc2();
      // should be identical to doc1 from before, including revisions
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
      // TODO test that target2 is 404
      // expectTargets(true, FR, target2);
      // use dao to check that doc2 is marked obsolete
      verifyObsoleteDocument(doc2.getName());
   }

   public void put1Delete1Put1() throws Exception
   {
      log.info("TEST: put1Put0Put1()");
      getZero();
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
      deleteDoc1(); // doc1 becomes obsolete, rev 2
      getZero();
      // TODO test that target1 is 404
      putDoc1(false); // doc1 resurrected, rev 3
      // doc1.setRevision(3);
      // tf1.setRevision(1);
      // tft1.setResourceRevision(1);
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
   }

   public void put1Delete1Put1a() throws Exception
   {
      log.info("TEST: put1Put0Put1a()");
      getZero();
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
      deleteDoc1(); // doc1 becomes obsolete, rev 2
      getZero();
      // TODO test that target1 is 404
      Resource doc1a = putDoc1a(false); // doc1 resurrected, rev 3
      // doc1a.setRevision(3);
      TextFlow tf1a = doc1a.getTextFlows().get(0);
      // tf1a.setRevision(3);
      TranslationsResource target1a = putTarget1a();
      // TextFlowTarget tft1a = getTextFlowTargets().get(0);
      // tft1a.setResourceRevision(3);
      expectDocs(true, false, doc1a);
      // TODO test that target1 is 404
      expectTargets(true, FR, target1a);
   }

   public void putPoPotGet() throws Exception
   {
      log.info("TEST: putPoPotGet()");
      getZero();
      Resource po1 = putPo1();
      expectDocs(false, false, po1);
   }

   public void put1Put1a() throws Exception
   {
      log.info("TEST: put1Then1a()");
      log.info("getZero()");
      getZero();
      log.info("putDoc1()");
      Resource doc1 = putDoc1(false);
      // doc1.setRevision(1);
      TextFlow tf1 = doc1.getTextFlows().get(0);
      // tf1.setRevision(1);
      TranslationsResource target1 = putTarget1();
      // TextFlowTarget tft1 = getTextFlowTargets().get(0);
      // tft1.setResourceRevision(1);
      log.info("expect doc1");
      expectDocs(true, false, doc1);
      expectTargets(true, DE, target1);
      // this should completely replace doc1's textflow FOOD with HELLO
      log.info("putDoc1a()");
      Resource doc1a = putDoc1a(false);
      // doc1a.setRevision(2);
      TextFlow tf1a = doc1a.getTextFlows().get(0);
      // tf1a.setRevision(2);
      TranslationsResource target1a = putTarget1a();
      // TextFlowTarget tft1a = getTextFlowTargets().get(0);
      // tft1a.setResourceRevision(2);
      log.info("expect doc1a");
      expectDocs(true, false, doc1a);
      // TODO test that target1 is 404
      expectTargets(true, FR, target1a);
      // use dao to check that the HTextFlow FOOD (from doc1) is marked obsolete
      log.info("verifyObsoleteResource");
      verifyObsoleteResource(doc1.getName(), "FOOD");
      log.info("putDoc1() again");
      putDoc1(false); // same as original doc1, but different doc rev
      // doc1.setRevision(3);
      log.info("expect doc1b");
      expectDocs(true, false, doc1);
      // TODO test that target1 is 404?
      expectTargets(true, DE, target1);
      // TODO test that target1A is 404
   }

   public void getBadProject() throws Exception
   {
      log.info("getBadProject()");
      ITranslationResources badTransResource = getClientRequestFactory().createProxy(ITranslationResources.class, createBaseURI(BAD_RESOURCE_PATH));
      ClientResponse<List<ResourceMeta>> response = badTransResource.get(null);
      assertThat(response.getStatus(), is(404));
   }

   // END of tests

   private void expectDocs(boolean checkRevs, boolean checkLinksIgnored, Resource... docs)
   {
      expectResourceMetas(checkRevs, docs);
      // FIXME check TextFlows
   }

   private void expectTargets(boolean checkRevs, LocaleId locale, TranslationsResource docs)
   {
      // expectResourceMetas(checkRevs, docs);
      // FIXME check TextFlowTargets
   }

   private void expectResourceMetas(boolean checkRevs, AbstractResourceMeta... docs)
   {
      ClientResponse<List<ResourceMeta>> response = transResource.get(null);

      assertThat(response.getStatus(), is(200));
      List<ResourceMeta> actualDocs = response.getEntity();
      assertThat(actualDocs, notNullValue());
      Set<String> expectedDocs = new TreeSet<String>();
      for (AbstractResourceMeta doc : docs)
      {
         String docVal = doc.getName();
         // FIXME
         // if (checkRevs)
         // docVal += ":"+doc.getRevision();
         expectedDocs.add(docVal);
      }
      // assertThat(actualDocs.size(), is(docs.length));
      Set<String> actualDocVals = new TreeSet<String>();
      for (ResourceMeta doc : actualDocs)
      {
         String docVal = doc.getName();
         // FIXME
         // if (checkRevs)
         // docVal += ":"+doc.getRevision();
         actualDocVals.add(docVal);
         log.debug("actual doc: " + doc.toString());
      }
      assertThat(actualDocVals, is(expectedDocs));
   }

   private void clearRevs(AbstractResourceMeta doc)
   {
      // FIXME
      // doc.setRevision(null);

      if (doc instanceof Resource)
      {
         Resource res = (Resource) doc;
         final List<TextFlow> textFlows = res.getTextFlows();
         if (textFlows != null)
            for (TextFlow tf : textFlows)
            {
               // FIXME
               // tf.setRevision(null);
            }
      }

   }
   //
   // private void clearRevs(List<TextFlowTarget> tfTargets)
   // {
   // if (tfTargets != null)
   // for (TextFlowTarget tft : tfTargets)
   // {
   // tft.setResourceRevision(null);
   // }
   // }

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
      // doc.setRevision(null);
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
      TextFlowTarget target = new TextFlowTarget(id);
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

      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("FOOD", "Sauerkraut", "translator comment");
      tr.getTextFlowTargets().add(target);


      PoTargetHeader targetHeader = tr.getExtensions(true).findOrAddByType(PoTargetHeader.class);
      targetHeader.setComment("target comment");
      List<HeaderEntry> entries = targetHeader.getEntries();
      entries.add(new HeaderEntry("Project-Id-Version", "ja"));

      transResource.putTranslations(id, DE, tr, extGettextComment);

      log.debug(doc.toString());
      Response response = transResource.putResource(id, doc, extGettextComment);
      assertThat(response.getStatus(), isOneOf(200, 201));
      return doc;
   }

   private Resource putDoc1(boolean putTarget)
   {
      String id = "foo.properties";
      Resource doc = newDoc(id, newTextFlow("FOOD", "Slime Mould", "slime mould comment"));
      Response response = transResource.putResource(id, doc, extComment);
      assertThat(response.getStatus(), isOneOf(200, 201));

      if (putTarget)
         putTarget1();

      return doc;
   }

   protected TranslationsResource putTarget1()
   {
      String id = "foo.properties";
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("FOOD", "Sauerkraut", null);
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, DE, tr, extGettextComment);
      return tr;
   }

   private Resource putDoc1a(boolean putTarget)
   {
      String id = "foo.properties";
      Resource doc = newDoc(id, newTextFlow("HELLO", "Hello World", null));
      Response response = transResource.putResource(id, doc, extComment);
      assertThat(response.getStatus(), isOneOf(200, 201));

      if (putTarget)
         putTarget1a();

      return doc;
   }

   protected TranslationsResource putTarget1a()
   {
      String id = "foo.properties";
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("HELLO", "Bonjour le Monde", "bon jour comment");
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, FR, tr, extGettextComment);
      return tr;
   }

   private void deleteDoc1()
   {
      deleteDoc("foo.properties");
   }

   private void deleteDoc2()
   {
      deleteDoc("test.properties");
   }

   protected void deleteDoc(String id)
   {
      Response response = transResource.deleteResource(id);
      assertThat(response.getStatus(), is(200));
   }

   private Resource postDoc2(boolean putTarget)
   {
      String id = "test.properties";
      Resource doc = newDoc(id, newTextFlow("HELLO", "Hello World", "hello comment"));
      Response response = transResource.post(doc, extComment);
      assertThat(response.getStatus(), is(201));

      if (putTarget)
         putTarget2();

      return doc;
   }

   protected TranslationsResource putTarget2()
   {
      String id = "test.properties";
      TranslationsResource tr = new TranslationsResource();
      TextFlowTarget target = newTextFlowTarget("HELLO", "Bonjour le Monde", null);
      tr.getTextFlowTargets().add(target);
      transResource.putTranslations(id, FR, tr, extGettextComment);
      return tr;
   }

   private void verifyObsoleteDocument(final String docID) throws Exception
   { // FIXME!
   // HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug,
   // iter);
   // HDocument hDocument = iteration.getAllDocuments().get(docID);
   // Assert.assertTrue(hDocument.isObsolete());
   }

   private void verifyObsoleteResource(final String docID, final String resourceID) throws Exception
   { // FIXME!
   // HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug,
   // iter);
   // HDocument hDocument = iteration.getAllDocuments().get(docID);
   // HTextFlow hResource = hDocument.getAllTextFlows().get(resourceID);
   // Assert.assertNotNull(hResource);
   // Assert.assertTrue(hResource.isObsolete());
   }
}