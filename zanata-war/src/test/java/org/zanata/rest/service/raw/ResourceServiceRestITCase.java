package org.zanata.rest.service.raw;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;

import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.service.ResourceTestObjectFactory;
import org.zanata.rest.service.ResourceTestUtil;
import org.zanata.rest.service.SourceDocResource;

import com.google.common.collect.ImmutableList;

public class ResourceServiceRestITCase extends SourceAndTranslationResourceRestBase {
    private final Logger log = LoggerFactory
            .getLogger(ResourceServiceRestITCase.class);

    private static final ResourceTestObjectFactory resourceTestFactory =
            new ResourceTestObjectFactory();

    private static final List<Resource> data = ImmutableList
            .<Resource> builder().add(
                    resourceTestFactory.getPotEntryHeaderTest(),
                    resourceTestFactory.getTextFlowCommentTest(),
                    resourceTestFactory.getPoHeaderTest(),
                    resourceTestFactory.getTextFlowTest())
            .build();

    private Resource sr;

    @Before
    public void setUp() {
        int randomData = (int) (Math.random() * data.size());
        log.debug("picking test data index: {}", randomData);
        sr = data.get(randomData);
    }

    @Test
    @RunAsClient
    public void testPutGetResourceWithExtension() {
        log.debug("put resource:" + sr.toString());
        getSourceDocResource().putResource(sr.getName(), sr, new StringSet(
                "gettext;comment"), false);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(
                        sr.getName(),
                        new StringSet("gettext;comment")));
        ResourceTestUtil.clearRevs(sr);
        ResourceTestUtil.clearRevs(get);
        assertThat(get, equalTo(sr));
    }

    @Test
    @RunAsClient
    public void testPutGetNoExtensionResource() {
        log.debug("put resource:" + sr.toString());
        getSourceDocResource().putResource(sr.getName(), sr, null, false);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(),
                        new StringSet("gettext;comment")));
        Resource base = resourceTestFactory.getTextFlowTest();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPutNoExtensionGetResource() {
        log.debug("put resource:" + sr.toString());
        getSourceDocResource().putResource(sr.getName(), sr, new StringSet(
                "gettext;comment"), false);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(), null));
        Resource base = resourceTestFactory.getTextFlowTest();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPutGetResource() {
        getSourceDocResource().putResource(sr.getName(), sr, null, false);
        Resource base = resourceTestFactory.getTextFlowTest();
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(), null));
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPostGetResource() {
        getSourceDocResource().post(sr, null, true);
        Resource base = resourceTestFactory.getTextFlowTest();
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(), null));
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPostGetResourceWithExtension() {
        getSourceDocResource().post(sr, new StringSet("gettext;comment"), true);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(),
                        new StringSet("gettext;comment")));
        ResourceTestUtil.clearRevs(sr);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + sr.toString());
        log.debug("actual:" + get.toString());
        assertThat(get.toString(), is(sr.toString()));
    }

    @Test
    @RunAsClient
    public void testPostNoExtensionGetResource() {
        log.debug("post resource:" + sr.toString());
        getSourceDocResource().post(sr, null, true);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(),
                        new StringSet("gettext;comment")));
        Resource base = resourceTestFactory.getTextFlowTest();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPostGetNoExtensionResource() {
        log.debug("post resource:" + sr.toString());
        getSourceDocResource().post(sr, new StringSet("gettext;comment"), true);
        Resource get = getResourceFromResponse(
                getSourceDocResource().getResource(sr.getName(), null));
        Resource base = resourceTestFactory.getTextFlowTest();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        assertThat(get.toString(), is(base.toString()));
    }

    @Test
    @RunAsClient
    public void testPutGetResourceMeta() {
        log.debug("test put get resource meta service");
        Resource res = resourceTestFactory.getTextFlowTest();
        getSourceDocResource().putResource(res.getName(), res, new StringSet(
                "gettext;comment"), false);
        ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
        getSourceDocResource().putResourceMeta(sr.getName(), sr, new StringSet(
                "gettext;comment"));
        log.debug("get resource meta");
        Response resourceGetResponse =
                getSourceDocResource()
                        .getResourceMeta(sr.getName(), new StringSet(
                                "gettext;comment"));
        ResourceMeta get = getResourceMetaFromResponse(resourceGetResponse);
        ResourceTestUtil.clearRevs(sr);
        ResourceTestUtil.clearRevs(get);
        assertThat(sr, equalTo(get));
    }

    @Test
    @RunAsClient
    public void testPutNoExtensionGetResourceMeta() {
        log.debug("test put get resource meta service");
        Resource res = resourceTestFactory.getTextFlowTest();
        getSourceDocResource().putResource(res.getName(), res, null, false);
        ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
        ResourceMeta base = resourceTestFactory.getResourceMeta();
        getSourceDocResource().putResourceMeta(sr.getName(), sr, null);
        log.debug("get resource meta");
        Response resourceGetResponse =
                getSourceDocResource()
                        .getResourceMeta(sr.getName(), new StringSet(
                                "gettext;comment"));
        ResourceMeta get = getResourceMetaFromResponse(resourceGetResponse);
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        assertThat(get, equalTo(base));
    }

    @Test
    @RunAsClient
    public void testPutGetNoExtensionResourceMeta() {
        log.debug("test put get resource meta service");
        Resource res = resourceTestFactory.getTextFlowTest();
        getSourceDocResource().putResource(res.getName(), res, null, false);
        ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
        ResourceMeta base = resourceTestFactory.getResourceMeta();
        getSourceDocResource().putResourceMeta(sr.getName(), sr, new StringSet(
                "gettext;comment"));
        log.debug("get resource meta");
        Response resourceGetResponse =
                getSourceDocResource().getResourceMeta(sr.getName(), null);
        ResourceMeta get = getResourceMetaFromResponse(resourceGetResponse);
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        assertThat(get, equalTo(base));
    }

    @Test
    @RunAsClient
    public void testDeleteResource() {
        Resource rs1 = resourceTestFactory.getTextFlowTest2();
        getSourceDocResource().post(rs1, null, true);
        Response resourceGetResponse =
                getSourceDocResource().deleteResource(rs1.getName());
        assertThat(resourceGetResponse.getStatus(),
                is(Status.OK.getStatusCode()));

        Resource rs2 = resourceTestFactory.getTextFlowTest();
        Response resourceGetResponse2 =
                getSourceDocResource().deleteResource(rs2.getName());
        assertThat(resourceGetResponse2.getStatus(),
                is(Status.NOT_FOUND.getStatusCode()));
    }

}
