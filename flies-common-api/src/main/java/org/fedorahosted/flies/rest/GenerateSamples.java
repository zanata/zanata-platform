package org.fedorahosted.flies.rest;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.ValidationException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.MediaTypes.Format;
import org.fedorahosted.flies.rest.dto.HasMediaType;
import org.fedorahosted.flies.rest.dto.ProjectRes;
import org.fedorahosted.flies.rest.dto.HasSample;
import org.fedorahosted.flies.rest.dto.v1.Person;
import org.fedorahosted.flies.rest.dto.v1.ResourcesList;
import org.fedorahosted.flies.rest.dto.v1.TranslationResource;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class GenerateSamples {

	private final ObjectMapper mapper;
	private final PrintStream out;

	private final PrintStream noCloseOut;
	
	public GenerateSamples(PrintStream out) {
		this.out = out;
		mapper = new ObjectMapper();
		mapper.getSerializationConfig().enable(Feature.INDENT_OUTPUT);
		
		noCloseOut = new PrintStream(out){
			@Override
			public void close() {
			}
		};
	}

	public void run() throws ValidationException, JsonGenerationException,
			JsonMappingException, IOException {

		// projects service
		write(ResourcesList.class);
		write(ProjectRes.class);
		
		write(Person.class);
		write(TranslationResource.class);
		write(ProjectRes.class);

	}

	private <T extends HasSample<T>> void write(Class<T> clazz) throws JsonGenerationException,
			JsonMappingException, IOException, ValidationException {
		
		T obj = create(clazz);
		validateEntity((Serializable)obj);
		out.println(obj.getClass().getCanonicalName());
		out.println();
		out.print("json:");
		if(obj instanceof HasMediaType) {
			out.print(" ");
			out.print(((HasMediaType)obj).getMediaType(Format.JSON));
		}
		out.println();
		mapper.writeValue(noCloseOut, obj);
		out.println();
		out.println();
		out.println("xml:");
		out.println();
		try {
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
					namespacePrefixMapper);
			m.marshal(obj, out);
			out.println();
			out.println("xml schema:");
			out.println();
			context.generateSchema(schemaOutputResolver);

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		out.println();
	}

	private final SchemaOutputResolver schemaOutputResolver = new SchemaOutputResolver() {

		@Override
		public Result createOutput(String namespaceUri, String suggestedFileName)
				throws IOException {
			StreamResult result = new StreamResult(noCloseOut);
			result.setSystemId("stdout");
			return result;

		}
	};

	private final NamespacePrefixMapper namespacePrefixMapper = new NamespacePrefixMapper() {

		@Override
		public String getPreferredPrefix(String namespaceUri,
				String suggestion, boolean requirePrefix) {
			if (namespaceUri == null || namespaceUri.equals("")) {
				return "tns";
			}
			if (namespaceUri.equalsIgnoreCase(Namespaces.FLIES)) {
				return "";
			}
			if (namespaceUri.equalsIgnoreCase(PoHeader.NAMESPACE)) {
				return "po";
			}
			if (namespaceUri
					.equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance")) {
				return "xsi";
			}
			return suggestion;
		}

		@Override
		public String[] getPreDeclaredNamespaceUris() {
			return new String[] {};
		}
	};

	private <T extends HasSample<T>> T create(Class<T> clazz) {
		try {
			return clazz.newInstance().createSample();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Serializable> void validateEntity(T entity) throws ValidationException {
		@SuppressWarnings("rawtypes")
		ClassValidator<T> validator = new ClassValidator(entity.getClass());
		if(validator.hasValidationRules() ) {
			InvalidValue[] invalidValues = validator.getInvalidValues(entity);
			if(invalidValues.length != 0) {
				StringBuilder message = new StringBuilder();
				message.append("Request body contains invalid values:\n");
				for(InvalidValue invalidValue : invalidValues) {
					message.append(invalidValue.getPropertyPath());
					message.append(">");
					message.append(invalidValue.getPropertyName());
					message.append(": ");
					message.append(invalidValue.getMessage());
					message.append("\n");
				}
				throw new ValidationException(message.toString());
			}
		}
	}
	
	public static void main(String[] args) throws ValidationException,
			JsonGenerationException, JsonMappingException, IOException {
		new GenerateSamples(System.out).run();
	}


}
