/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.zanata.rest.dto.DTOUtil;

/**
 * Provides utilities when testing Raw REST APIs.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class RawRestTestUtils {
    public static void assertJaxbUnmarshal(Response response,
            Class<?> jaxbType) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(jaxbType);
            Unmarshaller um = jc.createUnmarshaller();
            String entity = response.readEntity(String.class);
            um.unmarshal(new StringReader(entity));
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }
    }

    public static void assertJaxbUnmarshal(String entityString,
            Class<?> jaxbType) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(jaxbType);
            Unmarshaller um = jc.createUnmarshaller();
            um.unmarshal(new StringReader(entityString));
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }
    }

    public static void assertJsonUnmarshal(Response response,
            Class<?> jsonType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String entity = response.readEntity(String.class);
            mapper.readValue(entity, jsonType);
        } catch (IllegalStateException | IOException e) {
            throw new AssertionError(e);
        }
    }

    public static void assertJsonUnmarshal(String entityString,
            Class<?> jsonType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(entityString, jsonType);
        } catch (IllegalStateException | IOException e) {
            throw new AssertionError(e);
        }
    }

    public static void assertHeaderPresent(Response response,
            String headerName) {
        if (!response.getHeaders().containsKey(headerName)) {
            throw new AssertionError("Expected Http header '" + headerName
                    + "' in Response.");
        }
    }

    public static void assertHeaderValue(Response response,
            String headerName, String headerValue) {
        assertHeaderPresent(response, headerName);

        if (!response.getHeaders().getFirst(headerName).equals(headerValue)) {
            throw new AssertionError("Expected header '" + headerName
                    + "' to be '" + headerValue + "'; but instead got " + "'"
                    + response.getHeaders().get(headerName) + "'");
        }
    }

    public static <T> T
            jaxbUnmarshal(Response response, Class<T> jaxbType) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(jaxbType);
            Unmarshaller um = jc.createUnmarshaller();
            // um.setEventHandler( new
            // javax.xml.bind.helpers.DefaultValidationEventHandler() );
            String entity = response.readEntity(String.class);
            @SuppressWarnings("unchecked")
            T result = (T) um.unmarshal(new StringReader(entity));
            return result;
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }
    }

    public static <T> T
    jaxbUnmarshal(String entityString, Class<T> jaxbType) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(jaxbType);
            Unmarshaller um = jc.createUnmarshaller();
            // um.setEventHandler( new
            // javax.xml.bind.helpers.DefaultValidationEventHandler() );
            @SuppressWarnings("unchecked")
            T result = (T) um.unmarshal(new StringReader(entityString));
            return result;
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }
    }

    public static <T> T
            jsonUnmarshal(String entityString, Class<T> jsonType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(entityString, jsonType);
        } catch (IllegalStateException | IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String jaxbMarhsal(Object jaxbObject) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(jaxbObject.getClass());
            Marshaller m = jc.createMarshaller();
            // m.setEventHandler( new
            // javax.xml.bind.helpers.DefaultValidationEventHandler() );
            StringWriter sw = new StringWriter();
            m.marshal(jaxbObject, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }
    }

    public static String jsonMarshal(Object jsonObject) {
        return DTOUtil.toJSON(jsonObject);
    }
}
