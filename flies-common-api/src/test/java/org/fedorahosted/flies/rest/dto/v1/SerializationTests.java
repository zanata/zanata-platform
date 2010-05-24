package org.fedorahosted.flies.rest.dto.v1;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

public class SerializationTests {

	protected ObjectMapper mapper;

	@Before
	public void setup(){
		mapper = new ObjectMapper();
//		AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(); 
//		mapper.getDeserializationConfig().setAnnotationIntrospector(introspector); 
//		mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
	}
	
	
	private Person createPerson() {
		return new Person("id", "name"); 
	}
	
	@Test
	public void serializeAndDeserializePerson() throws JAXBException, JsonGenerationException, JsonMappingException, IOException {
		Person p = createPerson(); 
		JaxbTestUtil.validate(p);

		String output = mapper.writeValueAsString(p);
		
		Person p2 = mapper.readValue(output, Person.class);
		assertThat(p2 , notNullValue());
		JaxbTestUtil.validate(p2);
		
		p2 = JaxbTestUtil.roundTripXml(p);
		assertThat(p2 , notNullValue());
	}

	private PoHeader createPoHeader() {
		return new PoHeader("hello world");
	}
	
	@Test
	public void serializeAndDeserializeExtension() throws JsonGenerationException, JsonMappingException, IOException, JAXBException {
		Extension e = createPoHeader();
		JaxbTestUtil.validate(e);

		String output = mapper.writeValueAsString(e);
		Extension e2 = mapper.readValue(output, Extension.class);
		JaxbTestUtil.validate(e2);
		assertThat(e2, instanceOf(PoHeader.class));
		
		e2 = JaxbTestUtil.roundTripXml(e, Extension.class);
		assertThat(e2, instanceOf(PoHeader.class));
	}
	
	@Test
	public void serializeAndDeserializeTranslationResource() throws JsonGenerationException, JsonMappingException, IOException, JAXBException{
		TranslationResource res = new TranslationResource("id");
		res.getExtensions().add(new PoHeader());
		JaxbTestUtil.validate(res, PoHeader.class);
		
		String output = mapper.writeValueAsString(res);
		TranslationResource res2 = mapper.readValue(output, TranslationResource.class);
		
		assertThat( res2.getExtensions().size(), is(1));
		assertThat( res2.getExtensions().iterator().next(), instanceOf(PoHeader.class)); 
		
		res2 = JaxbTestUtil.roundTripXml(res, PoHeader.class);
		assertThat(res2, notNullValue());
		assertThat( res2.getExtensions().size(), is(1));
		assertThat( res2.getExtensions().iterator().next(), instanceOf(PoHeader.class)); 
	}
	
}
