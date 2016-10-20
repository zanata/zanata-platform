package org.zanata.rest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ZanataTest;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.test.CdiUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(CdiUnitRunner.class)
public class RestUtilsTest extends ZanataTest {

    @Inject
    RestUtils restUtils;

    @Produces
    public ValidatorFactory getValidatorFactory() {
        return Validation.buildDefaultValidatorFactory();
    }

    @Produces
    public Validator getValidator() {
        return getValidatorFactory().getValidator();
    }

    private static ResourceTestObjectFactory resourceTestFactory =
            new ResourceTestObjectFactory();
    private static TranslationsResourceTestObjectFactory transTestFactory =
            new TranslationsResourceTestObjectFactory();
    private static final Logger log = LoggerFactory.getLogger(RestUtilsTest.class);

    @Test
    public void getPoHeaderTest() throws Exception {
        testUnmarshallResource(resourceTestFactory.getPoHeaderTest());
    }

    @Test
    public void getPotEntryHeaderTest() throws Exception {
        testUnmarshallResource(resourceTestFactory.getPotEntryHeaderTest());
    }

    @Test
    public void getTextFlowCommentTest() throws Exception {
        testUnmarshallResource(resourceTestFactory.getTextFlowCommentTest());
    }

    @Test
    public void getTextFlowTest2() throws Exception {
        testUnmarshallResource(resourceTestFactory.getTextFlowTest2());
    }

    @Test
    public void getResourceMeta() throws Exception {
        testUnmarshallResourceMeta(resourceTestFactory.getResourceMeta());
    }

    @Test
    public void getPoHeaderResourceMeta() throws Exception {
        testUnmarshallResourceMeta(resourceTestFactory.getPoHeaderResourceMeta());
    }

    @Test
    public void getPoTargetHeaderTextFlowTargetTest()
            throws Exception {
        testUnmarshallTranslation(transTestFactory.getPoTargetHeaderTextFlowTargetTest());
    }

    @Test
    public void getTestObject()
            throws Exception {
        testUnmarshallTranslation(transTestFactory.getTestObject());
    }

    @Test
    public void getTestObject2()
            throws Exception {
        testUnmarshallTranslation(transTestFactory.getTestObject2());
    }

    @Test
    public void getTextFlowTargetCommentTest()
            throws Exception {
        testUnmarshallTranslation(transTestFactory.getTextFlowTargetCommentTest());
    }

    private void testUnmarshallResource(Resource res)
            throws UnsupportedEncodingException {
        // SeamMockClientExecutor test = new SeamMockClientExecutor();
        // ClientRequest client = test.createRequest("http://example.com/");
        // MultivaluedMap<String, String> header = client.getHeaders();
        testRestUtilUnmarshall(res, Resource.class);
    }

    private <T extends Serializable> void testRestUtilUnmarshall(T entity,
            Class<T> type) throws UnsupportedEncodingException {
        InputStream messageBody = null;
        try {
            String testStr = DTOUtil.toXML(entity);
            log.info("expect:" + testStr);

            messageBody = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
            T unmarshall =
                    unmarshall(type, messageBody,
                            MediaType.APPLICATION_XML_TYPE,
                            new Headers<>());
            log.info("got:" + DTOUtil.toXML(unmarshall));
            assertThat(DTOUtil.toXML(entity), is(testStr));
        } finally {
            if (messageBody != null) {
                try {
                    messageBody.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void testUnmarshallJsonTranslationsResource() {
        log.info("start jason");
        InputStream messageBody = null;
        try {
            // String testStr =
            // "{\"resId\":\"782f49c4e93c32403ba0b51821b38b90\",\"state\":\"Approved\",\"translator\":{},\"content\":\"title: ttff\",\"extensions\":[{\"object-type\": \"comment\",\"value\": \"testcomment\", \"space\": \"preserve\"}]}";
            String testStr =
                    "{\"textFlowTargets\":["
                            + "{\"resId\":\"rest1\","
                            + "\"state\":\"Approved\","
                            + "\"translator\":{"
                            + "\"email\":\"root@localhost\","
                            + "\"name\":\"Admin user\"},"
                            + "\"content\": \"<title>\u8bbf\u95ee\u5b58\u53d6\u63a7\u5236\u5217\u8868</title>\"},"
                            + "{\"resId\":\"rest2\","
                            + "\"state\":\"Approved\"," + "\"translator\":{"
                            + "\"email\":\"root@localhost\","
                            + "\"name\":\"Admin user\"},"
                            + "\"content\":\"hello world\"}]}";

            messageBody = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
            TranslationsResource unmarshall =
                    unmarshall(TranslationsResource.class,
                            messageBody, MediaType.APPLICATION_JSON_TYPE, null);
            log.info("got:" + unmarshall.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (messageBody != null) {
                try {
                    messageBody.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void testUnmarshallTranslation(TranslationsResource res)
            throws UnsupportedEncodingException {
        testRestUtilUnmarshall(res, TranslationsResource.class);
    }

    private void testUnmarshallResourceMeta(ResourceMeta res)
            throws UnsupportedEncodingException {
        testRestUtilUnmarshall(res, ResourceMeta.class);
    }

    @Test
    public void testVersion() throws Exception {
        VersionInfo ver = new VersionInfo(null, null, null);
        testRestUtilUnmarshall(ver, VersionInfo.class);
    }

    public <T> T unmarshall(Class<T> entityClass, InputStream is,
            MediaType requestContentType,
            MultivaluedMap<String, String> requestHeaders) {
        T entity;
        try {
            if (requestContentType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                ObjectMapper m = new ObjectMapper();
                entity = m.readValue(is, entityClass);
            } else if (requestContentType.equals(MediaType.APPLICATION_XML_TYPE)) {
                JAXBContext jc = JAXBContext.newInstance(entityClass);
                Unmarshaller um = jc.createUnmarshaller();
                entity = um.unmarshal(new StreamSource(is), entityClass).getValue();
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.debug("Bad Request: Unable to read request body:", e);
            throw new NoLogWebApplicationException(e, Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Unable to read request body: " + e.getMessage())
                    .build());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }

        restUtils.validateEntity(entity);

        return entity;
    }

}
