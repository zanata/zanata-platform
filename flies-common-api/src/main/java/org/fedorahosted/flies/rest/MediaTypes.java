package org.fedorahosted.flies.rest;

import javax.ws.rs.core.MediaType;

public class MediaTypes {

	private static final String XML = "+xml";
	private static final String JSON = "+json";
	
	private static final String APPLICATION_VND_FLIES = "application/vnd.flies";
	
	public static final String APPLICATION_FLIES_PROJECT = APPLICATION_VND_FLIES + ".project";
	public static final String APPLICATION_FLIES_PROJECT_XML = APPLICATION_FLIES_PROJECT + XML;
	public static final String APPLICATION_FLIES_PROJECT_JSON = APPLICATION_FLIES_PROJECT + JSON;

	public static final String APPLICATION_FLIES_PROJECTS = APPLICATION_VND_FLIES + ".projects";
	public static final String APPLICATION_FLIES_PROJECTS_XML = APPLICATION_FLIES_PROJECTS + XML;
	public static final String APPLICATION_FLIES_PROJECTS_JSON = APPLICATION_FLIES_PROJECTS + JSON;

	public static final String APPLICATION_FLIES_PROJECT_ITERATION = APPLICATION_VND_FLIES + ".project.iteration";
	public static final String APPLICATION_FLIES_PROJECT_ITERATION_XML = APPLICATION_FLIES_PROJECT_ITERATION + XML;
	public static final String APPLICATION_FLIES_PROJECT_ITERATION_JSON = APPLICATION_FLIES_PROJECT_ITERATION + JSON;

	public static final String APPLICATION_FLIES_PROJECT_ITERATIONS = APPLICATION_VND_FLIES + ".project.iterations";
	public static final String APPLICATION_FLIES_PROJECT_ITERATIONS_XML = APPLICATION_FLIES_PROJECT_ITERATIONS + XML;
	public static final String APPLICATION_FLIES_PROJECT_ITERATIONS_JSON = APPLICATION_FLIES_PROJECT_ITERATIONS + JSON;
	
	public static final String APPLICATION_FLIES_DOCUMENT = APPLICATION_VND_FLIES + ".document";
	public static final String APPLICATION_FLIES_DOCUMENT_XML = APPLICATION_FLIES_DOCUMENT + XML;
	public static final String APPLICATION_FLIES_DOCUMENT_JSON = APPLICATION_FLIES_DOCUMENT + JSON;

	public static final String APPLICATION_FLIES_DOCUMENTS = APPLICATION_VND_FLIES + ".documents";
	public static final String APPLICATION_FLIES_DOCUMENTS_XML = APPLICATION_FLIES_DOCUMENTS + XML;
	public static final String APPLICATION_FLIES_DOCUMENTS_JSON = APPLICATION_FLIES_DOCUMENTS + JSON;

	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCE = APPLICATION_VND_FLIES + ".document.resource";
	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCE_XML = APPLICATION_FLIES_DOCUMENT_RESOURCE + XML;
	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCE_JSON = APPLICATION_FLIES_DOCUMENT_RESOURCE + JSON;

	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCES = APPLICATION_VND_FLIES + ".document.resources";
	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCES_XML = APPLICATION_FLIES_DOCUMENT_RESOURCES + XML;
	public static final String APPLICATION_FLIES_DOCUMENT_RESOURCES_JSON = APPLICATION_FLIES_DOCUMENT_RESOURCES + JSON;

	/**
	 * Creates a format specific MediaType string given an existing media type
	 * 
	 * @param type the new type 
	 * @param from an existing media type with a format modifier such as xml or json 
	 * @return type with the format modifier from from
	 */
	public static String createFormatSpecificType(String type, MediaType from){
		StringBuilder str = new StringBuilder(type);
		String subtype = from.getSubtype();
		int plusIndex = subtype.indexOf('+');

		if(!(type.charAt(type.length()-1) == '/')) {
			str.append('+');
		}
		
		if(plusIndex != -1)
			str.append( subtype.substring( plusIndex+1 ) ); 
		else
			str.append(subtype);

		return str.toString();
	}
}
