package org.zanata.rest.dto.v1;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JaxbTestUtil {

    private static JAXBContext createJaxbContextFor(Object obj,
        Class<?>[] classes) throws JAXBException {
        Set<Class<?>> classSet = new HashSet<>();
        classSet.addAll(Arrays.asList(classes));
        classSet.add(obj.getClass());
        return JAXBContext.newInstance(classSet.toArray(new Class<?>[0]));
    }


    @SuppressWarnings("unchecked")
    public static <T> T roundTripXml(T obj, Class<?>... classes)
            throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext context = createJaxbContextFor(obj, classes);
        context.createMarshaller().marshal(obj, writer);
        writer.flush();
        System.out.println(writer.toString());
        obj =
                (T) context.createUnmarshaller().unmarshal(
                        new StringReader(writer.toString()));
        return obj;
    }

}
