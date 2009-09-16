package org.fedorahosted.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.ApiKeyHeaderDecorator;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Documents;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Test(groups = { "seam-tests" })
public class DocumentsServiceSeamTest extends DBUnitSeamTest {

	ClientRequestFactory clientRequestFactory;
	IDocumentsResource docsService;

	@BeforeClass
	public void prepareRestEasyClientFramework() throws Exception {

		ResteasyProviderFactory instance = ResteasyProviderFactory
				.getInstance();
		RegisterBuiltin.register(instance);

		clientRequestFactory = new ClientRequestFactory(
				new SeamMockClientExecutor(this), (URI)null);

		clientRequestFactory.getPrefixInterceptors().registerInterceptor(
				new ApiKeyHeaderDecorator("admin",
						"12345678901234567890123456789012"));

		docsService = clientRequestFactory
				.createProxy(IDocumentsResource.class, 
					"/restv1/projects/p/sample-project/iterations/i/1.1/documents");

	}

	@Override
	protected void prepareDBUnitOperations() {
	    beforeTestOperations.add(new DataSetOperation(
		    "org/fedorahosted/flies/test/model/DocumentsData.dbunit.xml",
		    DatabaseOperation.CLEAN_INSERT));
	}

	public void getZero() throws Exception {
	    expectDocs(0);
	}

	private void expectDocs(int expectDocs) {
	    ClientResponse<Documents> response = docsService.getDocuments();

	    assertThat(response.getStatus(), is(200));
	    assertThat(response.getEntity(), notNullValue());
	    assertThat(response.getEntity().getDocuments().size(), is(expectDocs));
	}
	
	private void putDoc1() {
	    Documents docs = new Documents();
	    ContentType contentType = ContentType.TextPlain;
	    Integer version = 1;
	    LocaleId lang = LocaleId.fromJavaName("es_ES");
	    docs.getDocuments().add(new Document("doc1", "doc1name", "path", contentType, version, lang));
	    Response response = docsService.put(docs);
	    assertThat(response.getStatus(), is(200));
	}
	
	private void postDoc2() {
	    Documents docs = new Documents();
	    ContentType contentType = ContentType.TextPlain;
	    Integer version = 1;
	    LocaleId lang = LocaleId.fromJavaName("es_ES");
	    docs.getDocuments().add(new Document("doc2", "doc2name", "path", contentType, version, lang));
	    Response response = docsService.post(docs);
	    assertThat(response.getStatus(), is(200));
	}
	
	public void putGet() throws Exception {
	    putDoc1();
	    expectDocs(1);
	}


	
	public void putPostGet() throws Exception {
	    putDoc1();
	    expectDocs(1);
	    postDoc2();
	    expectDocs(2);
	}

}
