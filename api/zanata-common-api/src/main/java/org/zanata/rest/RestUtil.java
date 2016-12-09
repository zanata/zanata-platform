package org.zanata.rest;

public class RestUtil {

    /*
     * See also org.zanata.rest.service.URIHelper#convertFromDocumentURIId(java.lang.String)
     */
    // discards a leading slash if any, then converts slashes to commas
    public static String convertToDocumentURIId(String id) {
        // NB this currently prevents us from allowing ',' in file names
        if (id.startsWith("/")) {
            return id.substring(1).replace('/', ',');
        }
        return id.replace('/', ',');
    }

}
