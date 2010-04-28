package org.fedorahosted.flies.rest.dto.v1;

import java.io.IOException;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.junit.Before;
import org.junit.Test;

public class JacksonSerializationTests {

	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setup(){
		AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(); 
		mapper.getDeserializationConfig().setAnnotationIntrospector(introspector); 
		mapper.getSerializationConfig().setAnnotationIntrospector(introspector); 
	}
	
	@Test
	public void serializePersonWithoutFailure() throws IOException{
		Person p = new Person("id", "name");
		String output = mapper.writeValueAsString(p);
	}
	
	@Test
	public void serializeTranslationResourceWithoutFailure() throws IOException{
		TranslationResource res = new TranslationResource("id");
		String output = mapper.writeValueAsString(res);
		System.out.println(output);
	}
	
}
