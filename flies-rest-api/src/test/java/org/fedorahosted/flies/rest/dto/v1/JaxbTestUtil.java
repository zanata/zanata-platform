package org.fedorahosted.flies.rest.dto.v1;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.ValidationException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JaxbTestUtil {

	public static void validate(Object obj, Class<?>... classes)
			throws ValidationException {
		try {
			int index = -1;
			for (int i = 0; i < classes.length; i++) {
				if (obj.getClass() == classes[i]) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				classes = Arrays.copyOf(classes, classes.length+1);
				classes[classes.length-1] = obj.getClass();
			}

			JAXBContext jc = JAXBContext.newInstance(classes);
			Marshaller m = jc.createMarshaller();
			final List<StringWriter> outs = new ArrayList<StringWriter>();
			jc.generateSchema(new SchemaOutputResolver() {
				@Override
				public Result createOutput(String namespaceUri,
						String suggestedFileName) throws IOException {
					StringWriter out = new StringWriter();
					outs.add(out);
					StreamResult streamResult = new StreamResult(out);
					streamResult.setSystemId("");
					return streamResult;
				}
			});
			StreamSource[] sources = new StreamSource[outs.size()];
			int i = 0;
			for(StringWriter writer : outs){
				writer.flush();
				//System.out.println(writer.toString());
				sources[i++] = new StreamSource(new StringReader(writer.toString()), "");
			}
			SchemaFactory sf = SchemaFactory
					.newInstance("http://www.w3.org/2001/XMLSchema");
			m.setSchema(sf.newSchema(sources));
			m.marshal(obj, new DefaultHandler());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			if (e instanceof ValidationException) {
				throw (ValidationException) e;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T roundTripXml(T obj, Class<?> ... classes) throws JAXBException {

		int index = -1;
		for (int i = 0; i < classes.length; i++) {
			if(classes[i] == obj.getClass()) {
				index = i;
				break;
			}
		}
		if(index == -1) {
			classes = Arrays.copyOf(classes, classes.length+1);
			classes[classes.length-1] = obj.getClass();
		}
		StringWriter writer = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(classes);
		context.createMarshaller().marshal(obj, writer);
		writer.flush();
		//System.out.println(writer.toString());
		obj = (T) context.createUnmarshaller().unmarshal(new StringReader(writer.toString()));
		return obj;
	}
	
}
