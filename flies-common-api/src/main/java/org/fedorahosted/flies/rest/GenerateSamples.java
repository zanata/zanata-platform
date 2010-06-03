package org.fedorahosted.flies.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.fedorahosted.flies.rest.dto.v1.Person;
import org.junit.Before;

import com.sun.xml.internal.ws.util.Pool.Unmarshaller;

public class GenerateSamples {

	private final ObjectMapper mapper;
	private final PrintStream out;
	
	public GenerateSamples(PrintStream out) {
		this.out = out;
		mapper = new ObjectMapper();
		mapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
	}

	
	public void run() throws ValidationException, JsonGenerationException, JsonMappingException, IOException {
		Person person = createPerson();
		JaxbUtil.validateXml(person, Person.class);
		
		write(person);
	}
	
	private <T> void write(T obj) throws JsonGenerationException, JsonMappingException, IOException {
		out.print(
			mapper.writeValueAsString(obj)
			);
		out.println();
		try{
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(obj, out);
		}
		catch(JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Person createPerson() {
		return new Person("me@example.com", "Mr. Example"); 
	}
	
	public static void main(String[] args) throws ValidationException, JsonGenerationException, JsonMappingException, IOException {
		new GenerateSamples(System.out).run();
	}
	
}
