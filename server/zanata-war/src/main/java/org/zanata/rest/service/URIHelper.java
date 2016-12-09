package org.zanata.rest.service;

import org.zanata.rest.RestUtil;

/**
 * @see {@link ZPathService}
 */
public final class URIHelper {

    private URIHelper() {
    }

    public static String getProject(String projectSlug) {
        return "/projects/p/" + projectSlug;
    }

    public static String getIteration(String projectSlug, String iterationSlug) {
        return getProject(projectSlug) + "/iterations/i/" + iterationSlug;
    }

    public static String getDocument(String projectSlug, String iterationSlug,
            String documentId) {
        return getIteration(projectSlug, iterationSlug) + "/r/"
                + RestUtil.convertToDocumentURIId(documentId);
    }

    /**
     * @see RestUtil#convertToDocumentURIId(String)
     */
    // just converts commas to slashes
    public static String convertFromDocumentURIId(String uriId) {
        return uriId.replace(',', '/');
    }

    /**
     * @deprecated Use {@link RestUtil#convertToDocumentURIId(String)} instead
     */
    public static String convertToDocumentURIId(String docId) {
        return RestUtil.convertToDocumentURIId(docId);
    }

}
