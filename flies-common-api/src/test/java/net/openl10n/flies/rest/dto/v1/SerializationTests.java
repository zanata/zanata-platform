package net.openl10n.flies.rest.dto.v1;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.ResourceType;
import net.openl10n.flies.rest.JaxbUtil;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.PoHeader;
import net.openl10n.flies.rest.dto.extensions.SimpleComment;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.Extension;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TextFlow;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

public class SerializationTests
{

   protected ObjectMapper mapper;

   @Before
   public void setup()
   {
      mapper = new ObjectMapper();
      // AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
      // mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
      // mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
   }

   private Person createPerson()
   {
      return new Person("id", "name");
   }

   @Test
   public void serializeAndDeserializePerson() throws JAXBException, JsonGenerationException, JsonMappingException, IOException
   {
      Person p = createPerson();
      JaxbUtil.validateXml(p);

      String output = mapper.writeValueAsString(p);

      Person p2 = mapper.readValue(output, Person.class);
      assertThat(p2, notNullValue());
      JaxbUtil.validateXml(p2);

      p2 = JaxbTestUtil.roundTripXml(p);
      assertThat(p2, notNullValue());
   }

   private PoHeader createPoHeader()
   {
      return new PoHeader("hello world");
   }

   @Test
   public void serializeAndDeserializeExtension() throws JsonGenerationException, JsonMappingException, IOException, JAXBException
   {
      // TODO are we actually trying to test serializing an extension where the type is not known?
      
      PoHeader e = createPoHeader();
      JaxbUtil.validateXml(e);

      String output = mapper.writeValueAsString(e);
      PoHeader e2 = mapper.readValue(output, PoHeader.class);
      JaxbUtil.validateXml(e2);
      assertThat(e2, instanceOf(PoHeader.class));

      e2 = JaxbTestUtil.roundTripXml(e, PoHeader.class);
      assertThat(e2, instanceOf(PoHeader.class));
   }

   @Test
   public void serializeAndDeserializeTranslationResource() throws JsonGenerationException, JsonMappingException, IOException, JAXBException
   {
      ResourceMeta res = new ResourceMeta("id");
      res.getExtensions(true).add(new PoHeader());
      JaxbUtil.validateXml(res, PoHeader.class);

      String output = mapper.writeValueAsString(res);
      ResourceMeta res2 = mapper.readValue(output, ResourceMeta.class);

      assertThat(res2.getExtensions().size(), is(1));
      assertThat(res2.getExtensions().iterator().next(), instanceOf(PoHeader.class));

      res2 = JaxbTestUtil.roundTripXml(res, PoHeader.class);
      assertThat(res2, notNullValue());
      assertThat(res2.getExtensions().size(), is(1));
      assertThat(res2.getExtensions().iterator().next(), instanceOf(PoHeader.class));
   }

   @Test
   public void serializeSourceResource() throws JsonGenerationException, JsonMappingException, IOException, JAXBException
   {
      Resource sourceResource = new Resource("Acls.pot");
      sourceResource.setType(ResourceType.FILE);
      sourceResource.setContentType(ContentType.PO);
      sourceResource.setLang(LocaleId.EN);

      System.out.println(mapper.writeValueAsString(sourceResource));

   }

   public void createTextFlow(){
      TextFlow tf = new TextFlow();
      SimpleComment<TextFlow> comment = new SimpleComment<TextFlow>("test");
      tf.getExtensions().add(comment);
   }
   
}
