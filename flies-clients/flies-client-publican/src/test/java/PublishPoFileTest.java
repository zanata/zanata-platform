
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.adapter.po.PoReader;
import org.fedorahosted.flies.rest.FliesClient;
import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.fedorahosted.flies.rest.dto.po.PoHeader;
import org.fedorahosted.flies.rest.dto.po.PotEntriesData;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.xml.sax.InputSource;

public class PublishPoFileTest {
	
	@Test
	public void publishASinglePOfile() throws IOException, JAXBException, URISyntaxException {

		Document doc = new Document("mydoc.po","mydoc.po", "/", PoReader.PO_CONTENT_TYPE);
		
		InputSource inputSource = new InputSource(
				"http://svn.fedorahosted.org/svn/Deployment_Guide/community/fc10/de-DE/Apache.po"
		);
		inputSource.setEncoding("utf8");
		
		PoReader poReader = new PoReader();

		System.out.println("parsing template");
		poReader.extractTemplate(doc, inputSource);
		
		System.out.println("starting REST client");
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "admin", "apikeyvalue");
		DocumentResource docResource = client.getProjectsResource().
		getProject("sample-project").
		getIteration("1.0").
		getDocuments().
		getDocument(doc.getId());

		System.out.println("Publishing document");
		Response response = docResource.put(doc);
		
		Status s = Status.fromStatusCode(response.getStatus());
		if(Status.CREATED == s ) {
			System.out.println("Document Created: " + doc.getId());
		}
		else{
			fail("Creation Failed with status: " + s);
		}
		
	}
	
}
