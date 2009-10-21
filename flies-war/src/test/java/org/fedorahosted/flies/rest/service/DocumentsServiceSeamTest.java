package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Test(groups = { "seam-tests" })
public class DocumentsServiceSeamTest extends DBUnitSeamTest {

	IDocumentsResource docsService;

	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {
		FliesClientRequestFactory clientRequestFactory = 
			new FliesClientRequestFactory("admin",
					"12345678901234567890123456789012", 
					new SeamMockClientExecutor(this));
		docsService = clientRequestFactory.getDocumentsResource(
				new URI("/restv1/projects/p/sample-project/iterations/i/1.1/documents"));
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
		expectDocs();
	}
	
	private void expectDocs(Document... docs) {
	    ClientResponse<Documents> response = docsService.getDocuments();
	    
	    assertThat(response.getStatus(), is(200));
	    assertThat(response.getEntity(), notNullValue());
	    Set<String> expected = new TreeSet<String>();
	    for (Document doc : docs) {
			expected.add(doc.toString());
		}
//	    assertThat(response.getEntity().getDocuments().size(), is(Arrays.asList(docs).size()));
	    Set<String> actual = new TreeSet<String>();
	    for (Document doc : response.getEntity().getDocuments()) {
	    	// leave links out of the XML
	    	doc.getLinks().clear();
			actual.add(doc.toString());
		}
//System.out.println("actual docs: "+docs);
	    assertThat(actual, is(expected));
	}
	
	private Document newDoc(String id, Resource... resources) {
		ContentType contentType = ContentType.TextPlain;
		Integer version = 1;
		Document doc = new Document(id, id+"name", id+"path", contentType, version, LocaleId.EN);
		for (Resource textFlow : resources) {
			doc.getResources(true).add(textFlow);
		}
		return doc;
	}
	
	private TextFlow newTextFlow(String id, String sourceContent, String sourceComment, String targetLocale, String targetContent, String targetComment) {
		TextFlow textFlow = new TextFlow(id, LocaleId.EN);
	    textFlow.setContent(sourceContent);
	    // FIXME disabled until we get comment persistence working
//	    if (sourceComment != null)
//	    	textFlow.getOrAddComment().setValue(sourceComment);
	    TextFlowTarget target = new TextFlowTarget(textFlow, LocaleId.fromJavaName(targetLocale));
	    target.setContent(targetContent);
	    // FIXME disabled until we get comment persistence working
//	    if (targetComment != null)
//	    	target.getOrAddComment().setValue(targetComment);
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
	    getZero();
	    Document doc1 = putDoc1();
	    expectDocs(doc1);
	}


	
	public void putPostGet() throws Exception {
	    getZero();
	    Document doc1 = putDoc1();
	    expectDocs(doc1);
	    Document doc2 = postDoc2();
	    expectDocs(doc1, doc2);
	}
	
	
	public void put2Then1() throws Exception {
	    getZero();
	    Document doc1 = putDoc1();
	    Document doc2 = postDoc2();
	    expectDocs(doc1, doc2);
	    // this put should have the effect of deleting doc2
		putDoc1();
	    expectDocs(doc1);
	}

	public void put1Then1a() throws Exception {
	    getZero();
	    Document doc1 = putDoc1();
	    expectDocs(doc1);
	    // this should completely replace doc1's textflow FOOD with HELLO
		Document doc1a = putDoc1a();
	    expectDocs(doc1a);
	}
	

	// TODO expect 404 for non-existent project
//	public void getBadProject() {
//		
//	}

}
