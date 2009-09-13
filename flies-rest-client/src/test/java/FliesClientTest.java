import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.rest.FliesClient;
import org.fedorahosted.flies.rest.client.DocumentResource;
import org.fedorahosted.flies.rest.client.ProjectIterationResource;
import org.fedorahosted.flies.rest.client.ProjectResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

public class FliesClientTest {
	
	@Test
	public void create20SampleProjects() throws URISyntaxException {
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "admin", "34567890123456789012345678901234");
		
		ProjectResource projectResource = client.getProjectResource();
		
		ClientResponse<Project> projectResponse = projectResource.getProject("sample-project");
		
		if (projectResponse.getResponseStatus().getStatusCode() < 399) {
			Project p = projectResponse.getEntity();
			System.out.println( p.getName() );
			p.getIterations().clear();
			p.setName( "replaced "+ p.getName());
			Response r = projectResource.updateProject("myproject", p);
			System.out.println("Completed with status: " + r.getStatus());
			
			for (int i = 1; i < 21; i++) {
				p = new Project("myxproject-"+i, "Project #"+i, "Sample Description #"+i);
				r = projectResource.addProject(p);
				Status s = Status.fromStatusCode(r.getStatus());
				if(Status.CREATED == s ) {
					System.out.println("Created project " + i);
				}
				else{
					System.err.println(i + "Failed with status: " + s);
				}
				
				ProjectIterationResource projectIterationResource = 
					client.getProjectIterationResource(p.getId());
				for (int j = 1; j < 3; j++) {
					ProjectIteration pIt = new ProjectIteration();
					pIt.setId("iteration-"+j);
					pIt.setName("Project Iteration #"+j);
					pIt.setSummary("A sample Iteration #"+j);
					r = projectIterationResource.addIteration(pIt);
					s = Status.fromStatusCode(r.getStatus());
					if(Status.CREATED == s ) {
						System.out.println("  Iteration Created: " + j);
					}
					else{
						System.err.println("  " + j + " Iteration Creation Failed with status: " + s);
					}
					
					String [] documentIds = {
							"my/document/doc1.txt", 
							"my/document/other.txt"};
					
					DocumentResource documentResource = client.getDocumentResource(p.getId(), pIt.getId());
					for (int k = 0; k < documentIds.length; k++) {
						Document doc = new Document(documentIds[k], ContentType.TextPlain);
						
						TextFlow tf = new TextFlow("tf1");
						tf.setContent("Hello World");
						doc.getResources().add(tf);
						
						r = documentResource.addDocument(doc);
						s = Status.fromStatusCode(r.getStatus());
						if(Status.CREATED == s ) {
							System.out.println("    Document Created: " + documentIds[k]);
						}
						else{
							System.err.println("    " + documentIds[k] + " Creation Failed with status: " + s);
						}
						
					}
					
				}
			}
		}
		
	}
}
