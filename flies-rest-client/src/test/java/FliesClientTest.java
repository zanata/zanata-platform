import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.rest.DocumentsResource;
import org.fedorahosted.flies.rest.FliesClient;
import org.fedorahosted.flies.rest.ProjectResource;
import org.fedorahosted.flies.rest.ProjectsResource;
import org.fedorahosted.flies.rest.client.IDocumentsResource;
import org.fedorahosted.flies.rest.client.IProjectResource;
import org.fedorahosted.flies.rest.client.IProjectsResource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectRes;
import org.fedorahosted.flies.rest.dto.ProjectType;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

public class FliesClientTest {
	
	@Test
	public void create20SampleProjects() throws URISyntaxException {
		FliesClient client = new FliesClient("http://localhost:8080/flies/seam/resource/restv1", "admin", "34567890123456789012345678901234");
		
		ProjectsResource projectsResource = client.getProjectsResource();
		
		ClientResponse<ProjectRes> projectResponse = projectsResource.getProjectResource("sample-project").get();
		
		if (projectResponse.getResponseStatus().getStatusCode() < 399) {
			ProjectRes p = new ProjectRes(projectResponse.getEntity());
			System.out.println( p.getName() );
			p.setName( "replaced "+ p.getName());
			Response r = projectsResource.getProjectResource("myproject").put(p);
			System.out.println("Completed with status: " + r.getStatus());
			
			for (int i = 1; i < 21; i++) {
				p = new ProjectRes("myxproject-"+i, "Project #"+i, "Sample Description #"+i, ProjectType.IterationProject);
				r = projectsResource.getProjectResource(p.getId()).put(p);
				Status s = Status.fromStatusCode(r.getStatus());
				if(Status.CREATED == s ) {
					System.out.println("Created project " + i);
				}
				else{
					System.err.println(i + "Failed with status: " + s);
				}
				
				ProjectResource projectResource = 
				    client.getProjectsResource().
				    getProjectResource(p.getId());
				for (int j = 1; j < 3; j++) {
					ProjectIteration pIt = new ProjectIteration();
					pIt.setId("iteration-"+j);
					pIt.setName("Project Iteration #"+j);
					pIt.setSummary("A sample Iteration #"+j);
					r = projectResource.getIterationResource(pIt.getId()).put(pIt);
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
					
					DocumentsResource documentResource = 
					    client.getProjectsResource().
					    getProjectResource(p.getId()).
					    getIterationResource(pIt.getId()).
					    getDocumentsResource();
					
					for (int k = 0; k < documentIds.length; k++) {
						Document doc = new Document(documentIds[k], ContentType.TextPlain);
						
						TextFlow tf = new TextFlow("tf1");
						tf.setContent("Hello World");
						doc.getResources(true).add(tf);
						
						r = documentResource.getDocumentResource(doc.getId()).put(doc);
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
