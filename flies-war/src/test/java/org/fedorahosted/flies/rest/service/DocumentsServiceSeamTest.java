package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HDocumentResource;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Test(groups = { "seam-tests" })
public class DocumentsServiceSeamTest extends DBUnitSeamTest {

    private final Logger log = LoggerFactory.getLogger(DocumentsServiceSeamTest.class);
    
	String projectSlug = "sample-project";
	String iter = "1.1";
	IDocumentsResource docsService;

	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {
		docsService = prepareRestEasyClientFramework(projectSlug, iter);
	}

	public IDocumentsResource prepareRestEasyClientFramework(String projectSlug, String iter) throws Exception {
		FliesClientRequestFactory clientRequestFactory = 
			new FliesClientRequestFactory("admin",
					"12345678901234567890123456789012", 
					new SeamMockClientExecutor(this));
		return clientRequestFactory.getDocumentsResource(
				new URI("/restv1/projects/p/"+projectSlug+"/iterations/i/"+iter+"/documents"));
	}
	
	@Override
	protected void prepareDBUnitOperations() {
	    beforeTestOperations.add(new DataSetOperation(
		    "org/fedorahosted/flies/test/model/DocumentsData.dbunit.xml",
		    DatabaseOperation.CLEAN_INSERT));
	    afterTestOperations.add(new DataSetOperation(
			    "org/fedorahosted/flies/test/model/DocumentsData.dbunit.xml",
			    DatabaseOperation.DELETE_ALL));
	}

	public void getZero() throws Exception {
//		log.info("getZero()");
		expectDocs();
	}
	
	private void expectDocs(Document... docs) {
	    ClientResponse<Documents> response = docsService.getDocuments();
	    
	    assertThat(response.getStatus(), is(200));
	    assertThat(response.getEntity(), notNullValue());
	    Set<String> expectedDocs = new TreeSet<String>();
	    for (Document doc : docs) {
			expectedDocs.add(doc.toString());
		}
//	    assertThat(response.getEntity().getDocuments().size(), is(Arrays.asList(docs).size()));
	    Set<String> actualDocs = new TreeSet<String>();
	    for (Document doc : response.getEntity().getDocuments()) {
	    	// leave links out of the XML
	    	doc.getLinks().clear();
			actualDocs.add(doc.toString());
		}
//System.out.println("actual docs: "+docs);
	    assertThat(actualDocs, is(expectedDocs));
	}
	
	private Document newDoc(String id, DocumentResource... resources) {
		ContentType contentType = ContentType.TextPlain;
		Integer revision = null;
		Document doc = new Document(id, id+"name", id+"path", contentType, revision, LocaleId.EN);
		for (DocumentResource textFlow : resources) {
			doc.getResources(true).add(textFlow);
		}
		return doc;
	}
	
	private TextFlow newTextFlow(String id, String sourceContent, String sourceComment, String targetLocale, String targetContent, String targetComment) {
		TextFlow textFlow = new TextFlow(id, LocaleId.EN);
	    textFlow.setContent(sourceContent);
	    if (sourceComment != null)
	    	textFlow.getOrAddComment().setValue(sourceComment);
	    TextFlowTarget target = new TextFlowTarget(textFlow, LocaleId.fromJavaName(targetLocale));
	    target.setContent(targetContent);
	    if (targetComment != null)
	    	target.getOrAddComment().setValue(targetComment);
		textFlow.addTarget(target);
		return textFlow;
	}

	private Document putDoc1() {
		Documents docs = new Documents();
		Document doc = newDoc("foo.properties", 
				newTextFlow("FOOD", "Slime Mould", "slime mould comment", "de_DE", "Sauerkraut", null));
		docs.getDocuments().add(doc);
		Response response = docsService.put(docs);
		assertThat(response.getStatus(), is(200));
		return doc;
	}
	
	private Document putDoc1a() {
		Documents docs = new Documents();
		Document doc = newDoc("foo.properties", 
	    		newTextFlow("HELLO", "Hello World", null, "fr", "Bonjour le Monde", "bon jour comment"));
		docs.getDocuments().add(doc);
		Response response = docsService.put(docs);
		assertThat(response.getStatus(), is(200));
		return doc;
	}
	
	private Document postDoc2() {
	    Documents docs = new Documents();
	    Document doc = newDoc("test.properties",
	    		newTextFlow("HELLO", "Hello World", "hello comment", "fr", "Bonjour le Monde", null));
		docs.getDocuments().add(doc);
	    Response response = docsService.post(docs);
	    assertThat(response.getStatus(), is(200));
		return doc;
	}
	
	public void putGet() throws Exception {
		log.info("putGet()");
	    getZero();
	    Document doc1 = putDoc1();
	    doc1.setRevision(1);
	    expectDocs(doc1);
	}


	
	public void putPostGet() throws Exception {
		log.info("putPostGet()");
	    getZero();
	    Document doc1 = putDoc1();
	    doc1.setRevision(1);
	    expectDocs(doc1);
	    Document doc2 = postDoc2();
	    doc2.setRevision(1);
	    expectDocs(doc1, doc2);
	}
	
	
	public void put2Then1() throws Exception {
		log.info("put2Then1()");
	    getZero();
	    Document doc1 = putDoc1();
	    doc1.setRevision(1);
	    Document doc2 = postDoc2();
	    doc2.setRevision(1);
	    expectDocs(doc1, doc2);
	    // this put should have the effect of deleting doc2
		Document doc1again = putDoc1();
		// should be identical to doc1 from before, so rev still equals 1
		doc1again.setRevision(1);
	    expectDocs(doc1again);
	    // use dto to check that doc2 is marked obsolete
	    verifyObsoleteDocument(doc2.getId());
	}

	public void put1Then1a() throws Exception {
		log.info("TEST: put1Then1a()");
		log.info("getZero()");
	    getZero();
	    log.info("putDoc1()");
	    Document doc1 = putDoc1();
	    doc1.setRevision(1);
	    log.info("expect doc1");
	    expectDocs(doc1);
	    // this should completely replace doc1's textflow FOOD with HELLO
	    log.info("putDoc1a()");
		Document doc1a = putDoc1a();
		doc1a.setRevision(2);
		log.info("expect doc1a");
	    expectDocs(doc1a);
	    // use dto to check that the HTextFlow FOOD (from doc1) is marked obsolete
		// FIXME breaking test disabled
	    log.info("verifyObsoleteResource");
	    verifyObsoleteResource(doc1.getId(), "FOOD");
	    log.info("putDoc1() again");
	    Document doc1again = putDoc1();
	    doc1again.setRevision(3);
	    log.info("expect doc1again");
	    expectDocs(doc1again);
	}
	

	private void verifyObsoleteDocument(final String docID) throws Exception {        
		new FacesRequest() {
            protected void invokeApplication() throws Exception {
            	ProjectContainerDAO containerDAO = (ProjectContainerDAO) getInstance("projectContainerDAO");
                HProjectContainer container = containerDAO.getBySlug(projectSlug, iter);
				HDocument hDocument = container.getAllDocuments().get(docID);
                Assert.assertTrue(hDocument.isObsolete());
            }
        }.run();
	}
	
	private void verifyObsoleteResource(final String docID, final String resourceID) throws Exception {        
		new FacesRequest() {
			protected void invokeApplication() throws Exception {
				ProjectContainerDAO containerDAO = (ProjectContainerDAO) getInstance("projectContainerDAO");
				HProjectContainer container = containerDAO.getBySlug(projectSlug, iter);
				HDocument hDocument = container.getAllDocuments().get(docID);
				HDocumentResource hResource = hDocument.getAllResources().get(resourceID);
				Assert.assertNotNull(hResource);
				Assert.assertTrue(hResource.isObsolete());
			}
		}.run();
	}
	
	// expect 404 for non-existent project
	public void getBadProject() throws Exception {
		log.info("getBadProject()");
		IDocumentsResource nonexistentDocsService = prepareRestEasyClientFramework("nonexistentProject", "99.9");
		ClientResponse<Documents> response = nonexistentDocsService.getDocuments();
		assertThat(response.getStatus(), is(404));
	}

}
