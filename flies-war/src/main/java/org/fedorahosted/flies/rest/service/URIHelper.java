package org.fedorahosted.flies.rest.service;

public final class URIHelper {

	private URIHelper() {
	}

	public static String getProject(String projectSlug) {
		return "projects/p/" + projectSlug;
	}

	public static String getIteration(String projectSlug, String iterationSlug) {
		return getProject(projectSlug) + "/iterations/i/" + iterationSlug;
	}

	public static String getDocument(String projectSlug, String iterationSlug,
			String documentId) {
		return getIteration(projectSlug, iterationSlug) + "/documents/d/"
				+ convertToDocumentURIId(documentId);
	}

	public static String convertFromDocumentURIId(String uriId) {

		return "/" + uriId.replace(',', '/');
	}

	public static String convertToDocumentURIId(String id) {
		// NB this currently prevents us from allowing ',' in file names
		if (id.startsWith("/")) {
			return id.substring(1).replace('/', ',');
		}
		return id.replace('/', ',');
	}

}
