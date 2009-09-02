

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
import org.xml.sax.InputSource;

public class PublishPoFileTest {
	
	@Test
	public void publishASinglePOfile() throws IOException, JAXBException, URISyntaxException {

		Document doc = new Document("doc1","mydoc.doc", "/", PoReader.PO_CONTENT_TYPE);
		
		InputSource inputSource = new InputSource(
				//new File("/home/asgeirf/projects/gitsvn/Deployment_Guide/pt-BR/SELinux_Background.po").toURI().toString()
				"http://svn.fedorahosted.org/svn/Deployment_Guide/community/fc10/de-DE/Apache.po"
		);
		inputSource.setEncoding("utf8");
		
		PoReader poReader = new PoReader();

		System.out.println("parsing template");
		poReader.extractTemplate(doc, inputSource);
		
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "bob");
		
		ProjectResource projectResource = client.getProjectResource();
		
		DocumentResource docResource = client.getDocumentResource("sample-project", "1.0");
		Response response = docResource.addDocument(doc);
		Status s = Status.fromStatusCode(response.getStatus());
		if(Status.CREATED == s ) {
			System.out.println("Document Created: " + doc.getId());
		}
		else{
			System.err.println("Creation Failed with status: " + s);
		}
		
		ClientResponse<Project> projectResponse = projectResource.getProject("sample-project");
		
		
	}
}
