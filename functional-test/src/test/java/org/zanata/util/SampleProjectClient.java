package org.zanata.util;

import java.util.Set;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.SampleProjectResource;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SampleProjectClient {

    private static void checkAndReleaseConnection(Response response1) {
        ClientResponse response = (ClientResponse) response1;
        assertThat(response.getStatus(), Matchers.equalTo(200));
        response.releaseConnection();
    }

    private static ClientRequest createRequest(String path) {
        ClientRequest clientRequest =
                new ClientRequest(
                        PropertiesHolder.getProperty(Constants.zanataInstance
                                .value()) + "rest/test/data/sample" + path);
        // having null username will bypass ZanataRestSecurityInterceptor
        // clientRequest.header("X-Auth-User", null);
        clientRequest.getHeaders().remove("X-Auth-User");
        clientRequest.header("X-Auth-Token",
                PropertiesHolder.getProperty(Constants.zanataApiKey.value()));
        clientRequest.header("Content-Type", "application/xml");
        return clientRequest;
    }

    public static void deleteExceptEssentialData() throws Exception {
        checkAndReleaseConnection(createRequest("").delete());
    }

    public static void makeSampleUsers() throws Exception {
        checkAndReleaseConnection(createRequest("/users").put());
    }

    /**
     * @param username
     *            username to join language team
     * @param localesCSV
     *            locale ids separated by comma
     */
    public static void userJoinsLanguageTeam(String username,
            String localesCSV) throws Exception {
        ClientRequest request =
                createRequest("/accounts/u/" + username + "/languages");

        checkAndReleaseConnection(request.queryParameter("locales", localesCSV)
                .put());
    }

    public static void makeSampleProject() throws Exception {
        checkAndReleaseConnection(createRequest("/project").put());
    }

    public static void makeSampleLanguages() throws Exception {
        checkAndReleaseConnection(createRequest("/languages").put());
    }

}
