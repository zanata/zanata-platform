package org.zanata.rest;

import javax.ws.rs.core.Response;

public class RestUtil {

    public static String convertToDocumentURIId(String docId) {
        // NB this currently prevents us from allowing ',' in file names
        if (docId.startsWith("/")) {
            return docId.substring(1).replace('/', ',');
        }
        return docId.replace('/', ',');
    }

    public static String convertFromDocumentURIId(String docIdWithNoSlash) {
        return docIdWithNoSlash.replace(',', '/');
    }

    public static boolean isNotFound(Response response) {
        return response.getStatus() ==
                Response.Status.NOT_FOUND.getStatusCode();
    }

}
