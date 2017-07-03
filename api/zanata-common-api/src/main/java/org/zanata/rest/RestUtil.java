package org.zanata.rest;

import javax.ws.rs.core.Response;

public class RestUtil {

    public static String convertToDocumentURIId(String id) {
        // NB this currently prevents us from allowing ',' in file names
        if (id.startsWith("/")) {
            return id.substring(1).replace('/', ',');
        }
        return id.replace('/', ',');
    }

    public static boolean isNotFound(Response response) {
        return response.getStatus() ==
                Response.Status.NOT_FOUND.getStatusCode();
    }

}
