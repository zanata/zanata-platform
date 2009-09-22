package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.client.ContentQualifier;
import org.fedorahosted.flies.rest.client.IDocumentResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


@Test(groups={"seam-tests"},suiteName="DocumentService")
public class DocumentServiceSeamTest extends DBUnitSeamTest{
	
	ClientRequestFactory clientRequestFactory;
	URI baseUri = URI.create("/restv1/");
	
	private static final String url = "projects/p/sample-project/iterations/i/1.0/documents/d/";
	
	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = 
			new ClientRequestFactory(
					new SeamMockClientExecutor(this), baseUri);
		
		clientRequestFactory.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator("admin", "12345678901234567890123456789012"));

		
	}
	
	private IDocumentResource getDocumentService(String docId){
		return clientRequestFactory.createProxy(IDocumentResource.class, baseUri.resolve(url).resolve(docId));
		
	}
	
	@Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
        		new DataSetOperation("META-INF/testdata/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
        beforeTestOperations.add(
        		new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
        beforeTestOperations.add(
                new DataSetOperation("org/fedorahosted/flies/rest/service/DocumentTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
        
    }


	public void getDocumentThatDoesntExist(){
		IDocumentResource documentResource = getDocumentService("my,doc,does,not,exist.txt");
		assertThat ( documentResource.get(null).getResponseStatus(), is(Status.NOT_FOUND) ); 
	}

	public void getDocument() throws URIException {
		String docUri = "my,path,document.txt";
		IDocumentResource documentResource = getDocumentService(docUri);
		ClientResponse<Document> response = documentResource.get(null);
		assertThat( response.getResponseStatus(), is(Status.OK) ) ;
		Document doc = response.getEntity();
		assertThat( doc.getId(), is("/my/path/document.txt") );
		assertThat( doc.getName(), is("document.txt") );
		assertThat( doc.getContentType(), is(ContentType.TextPlain) );
		assertThat( doc.getLang(), is(LocaleId.EN_US) );
		assertThat( doc.getVersion(), is(1) );
		assertThat( doc.getResources().isEmpty(), is(true) );

		Link link = doc.findLinkByRel(Relationships.SELF);
		assertThat( link, notNullValue() );
		assertThat( URIUtil.decode(link.getHref().toString()), endsWith(url+docUri) );
		
		link = doc.findLinkByRel(Relationships.DOCUMENT_CONTAINER);
		assertThat( link, notNullValue() );
		assertThat( link.getHref().toString(), endsWith("iterations/i/1.0") );
	}
	
	public void getDocumentWithResources() throws URIException {
		IDocumentResource documentResource = getDocumentService("my,path,document.txt");
		ClientResponse<Document> response = documentResource.get( ContentQualifier.ALL );
		assertThat( response.getResponseStatus(), is(Status.OK) ) ;
		Document doc = response.getEntity();
		assertThat( doc.getResources().size(), is(1) );

		response = documentResource.get( ContentQualifier.SOURCE );
		assertThat( response.getResponseStatus(), is(Status.OK) ) ;
		doc = response.getEntity();
		assertThat("Should have one resource", doc.getResources().size(), is(1) );
		assertThat("No targets should be included", ((TextFlow)doc.getResources().get(0)).getExtension(TextFlowTargets.class), nullValue() );

		LocaleId nbLocale = new LocaleId("nb-NO");
		response = documentResource.get( ContentQualifier.fromLocales(nbLocale));
		assertThat( response.getResponseStatus(), is(Status.OK) ) ;
		doc = response.getEntity();
		assertThat("should have one resource", doc.getResources().size(), is(1) );
		
		LocaleId deLocale = new LocaleId("de-DE");
		response = documentResource.get( ContentQualifier.fromLocales( nbLocale, deLocale));
		assertThat( response.getResponseStatus(), is(Status.OK) ) ;
		doc = response.getEntity();
		List<Resource> resources = doc.getResources(); 
		assertThat( resources.size(), is(1) );
		TextFlow tf = (TextFlow) resources.get(0);
		assertThat( tf, notNullValue());
		assertThat("should have a textflow with this id", tf.getId(), is("tf1") );

		TextFlowTarget tfTarget = tf.getTarget(nbLocale);
		assertThat("expected nb-NO target", tfTarget, notNullValue());
		assertThat("expected translation for nb-NO", tfTarget.getContent(), is("hei verden"));
		
		tfTarget = tf.getTarget(deLocale);
		assertThat("exected de-DE target", tfTarget, notNullValue());
		assertThat("expected translation for de-DE",  tfTarget.getContent(), is("hello welt"));
	}
	
	public void putNewDocument() {
		String docUrl = "my,fancy,document.txt";
		IDocumentResource documentResource = getDocumentService(docUrl);
		Document doc = new Document("/my/fancy/document.txt", ContentType.TextPlain);
		Response response = documentResource.put(doc);

		assertThat( response.getStatus(), is(Status.CREATED.getStatusCode()) );
		// todo, change to clientresponse<string>
		ClientResponse<Document> documentResponse = documentResource.get(null);
		
		assertThat( documentResponse.getResponseStatus(), is(Status.OK) );
		
		doc = documentResponse.getEntity(); 
		assertThat( doc.getVersion(), is(1) );
		Link link = doc.findLinkByRel(Relationships.SELF); 
		assertThat( link, notNullValue() );
		assertThat( link.getHref().toString(), endsWith(url+docUrl) );
		
		link = doc.findLinkByRel(Relationships.DOCUMENT_CONTAINER); 
		assertThat( link, notNullValue() );
		assertThat( link.getType(), is( MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML) );
	}
}
