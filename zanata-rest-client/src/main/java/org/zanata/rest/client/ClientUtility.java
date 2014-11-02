package org.zanata.rest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import com.google.common.base.Strings;

public class ClientUtility {
    public static void checkResult(ClientResponse<?> response) {
        checkResult(response, null);
    }

    public static void checkResult(ClientResponse<?> response, URI uri) {
        Response.Status responseStatus = response.getResponseStatus();
        int statusCode = response.getStatus();

        if (responseStatus == Response.Status.UNAUTHORIZED) {
            throw new RuntimeException("Incorrect username/password");
        } else if (responseStatus == Response.Status.SERVICE_UNAVAILABLE) {
            throw new RuntimeException("Service is currently unavailable. " +
                    "Please check outage notification or try again later.");
        } else if (responseStatus == Response.Status.MOVED_PERMANENTLY
                || statusCode == 302 ) {
            // if server returns a redirect (most likely due to http to https
            // redirect), we don't want to bury this information in a xml
            // marshalling exception.
            String movedTo = response.getHeaderString("Location");

            String message;
            if (!Strings.isNullOrEmpty(movedTo)) {
                String baseUrl = getBaseURL(movedTo);
                message = "Server returned a redirect to:" + baseUrl +
                        ". You must change your url option or config file.";
            } else {
                message = "Server returned a redirect. You must change your url option or config file.";
            }
            throw new RuntimeException(message);
        } else if (statusCode >= 399) {
            String annotString = "";
            String uriString = "";
            String entity = "";
            if (response instanceof BaseClientResponse) {
                BaseClientResponse<?> resp = (BaseClientResponse<?>) response;
                annotString =
                        ", annotations: "
                                + Arrays.asList(resp.getAnnotations())
                                        .toString();
            }
            if (uri != null) {
                uriString = ", uri: " + uri;
            }
            try {
                entity = ": " + response.getEntity(String.class);
            } finally {
                // ignore
            }
            String msg =
                    "operation returned "
                            + statusCode
                            + " ("
                            + Response.Status.fromStatusCode(statusCode) + ")"
                            + entity + uriString
                            + annotString;
            throw new RuntimeException(msg);
        }
    }

    public static String getBaseURL(String movedTo) {
        try {
            URL url = new URI(movedTo).toURL();
            int pathIndex = movedTo.lastIndexOf(url.getPath());
            return movedTo.substring(0, pathIndex) + "/";
        }
        catch (MalformedURLException | URISyntaxException e) {
            return movedTo;
        }
    }

    public static void checkResultAndReleaseConnection(
            ClientResponse<?> clientResponse) {
        checkResult(clientResponse, null);
        clientResponse.releaseConnection();
    }
}
