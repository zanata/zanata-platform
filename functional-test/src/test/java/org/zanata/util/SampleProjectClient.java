package org.zanata.util;

import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.zanata.rest.SampleProjectResource;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SampleProjectClient {
    static final SampleProjectResource RESOURCE = ProxyFactory
            .create(SampleProjectResource.class,
                    PropertiesHolder.getProperty("zanata.instance.url")
                            + "seam/resource/restv1");

    public static void checkAndReleaseConnection(Response response1) {
        ClientResponse response =
                (ClientResponse) response1;
        assertThat(response.getStatus(), Matchers.equalTo(200));
        response.releaseConnection();
    }

}
