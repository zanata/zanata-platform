package org.example.test;


import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fedorahosted.flies.api.projects.Project;
import org.fedorahosted.flies.api.projects.Project.ProjectType;
import org.fedorahosted.flies.api.projects.iprojects.IterationProject;
import org.fedorahosted.flies.api.projects.iprojects.ProjectIteration;
import org.fedorahosted.flies.api.projects.iprojects.IterationsType;

public class Hello {

	public static Project createSampleProject(){
		Project p = new Project();
		setCommonProperties(p);
		p.setProjectType(ProjectType.DEFAULT);
		return p;
	}

	private static void setCommonProperties(Project p){
		p.setName("sample name");
		p.setDescription("sample description");
		p.setId("sample-project");
	}
	
	public static Project createSampleIterationProject(){
		Project p = new Project();
		setCommonProperties(p);
		ProjectIteration iteration = new ProjectIteration();
		iteration.setId("my-iteration");
		iteration.setName("my iteration");
		
		IterationsType iterations = new IterationsType();
		iterations.getIterations().add(iteration);
		
		p.getExtensions().add(iterations);
		//p.getIterations().add(iteration);
		return p;
	}
	public static void main(String[] args) {
		try {
			JAXBContext jc = JAXBContext.newInstance(Project.class);

			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			Unmarshaller um = jc.createUnmarshaller();
			
			StringWriter strWriter = new StringWriter();
			m.marshal( createSampleProject(), strWriter);
			StringReader strReader = new StringReader(strWriter.toString());
			Object obj = um.unmarshal(strReader);
			System.out.println("obj is of type " + obj.getClass().getName());
			m.marshal(obj, System.out);
			System.out.println();
			
			strWriter = new StringWriter();
			m.marshal(createSampleIterationProject(), strWriter);
			strReader = new StringReader(strWriter.toString());
			obj = um.unmarshal(strReader);
			System.out.println("obj is of type " + obj.getClass().getName());
			m.marshal(obj, System.out);
			System.out.println();
			
			
		} 
		catch (JAXBException jbe) {
			jbe.printStackTrace();
		}
	}
}
