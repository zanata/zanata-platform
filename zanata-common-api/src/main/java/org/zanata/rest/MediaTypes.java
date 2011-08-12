package org.zanata.rest;

import javax.ws.rs.core.MediaType;

public class MediaTypes
{

   public static enum Format
   {
      XML("xml"), JSON("json");

      private final String format;

      private Format(String format)
      {
         this.format = format;
      }

      public String toString()
      {
         return "+" + format;
      };
   }

   private static final String XML = "+xml";
   private static final String JSON = "+json";

   private static final String APPLICATION_VND_ZANATA = "application/vnd.zanata";

   public static final String APPLICATION_ZANATA_PROJECT = APPLICATION_VND_ZANATA + ".project";
   public static final String APPLICATION_ZANATA_PROJECT_XML = APPLICATION_ZANATA_PROJECT + XML;
   public static final String APPLICATION_ZANATA_PROJECT_JSON = APPLICATION_ZANATA_PROJECT + JSON;

   public static final String APPLICATION_ZANATA_PROJECTS = APPLICATION_VND_ZANATA + ".projects";
   public static final String APPLICATION_ZANATA_PROJECTS_XML = APPLICATION_ZANATA_PROJECTS + XML;
   public static final String APPLICATION_ZANATA_PROJECTS_JSON = APPLICATION_ZANATA_PROJECTS + JSON;

   public static final String APPLICATION_ZANATA_PROJECT_ITERATION = APPLICATION_VND_ZANATA + ".project.iteration";
   public static final String APPLICATION_ZANATA_PROJECT_ITERATION_XML = APPLICATION_ZANATA_PROJECT_ITERATION + XML;
   public static final String APPLICATION_ZANATA_PROJECT_ITERATION_JSON = APPLICATION_ZANATA_PROJECT_ITERATION + JSON;

	public static final String APPLICATION_ZANATA_ACCOUNT = APPLICATION_VND_ZANATA + ".account";
	public static final String APPLICATION_ZANATA_ACCOUNT_XML = APPLICATION_ZANATA_ACCOUNT + XML;
	public static final String APPLICATION_ZANATA_ACCOUNT_JSON = APPLICATION_ZANATA_ACCOUNT + JSON;

   public static final String APPLICATION_ZANATA_VERSION = APPLICATION_VND_ZANATA + ".Version";
   public static final String APPLICATION_ZANATA_VERSION_XML = APPLICATION_ZANATA_VERSION + XML;
   public static final String APPLICATION_ZANATA_VERSION_JSON = APPLICATION_ZANATA_VERSION + JSON;

   public static final String APPLICATION_ZANATA_GLOSSARY = APPLICATION_VND_ZANATA + ".glossary";
   public static final String APPLICATION_ZANATA_GLOSSARY_XML = APPLICATION_ZANATA_GLOSSARY + XML;
   public static final String APPLICATION_ZANATA_GLOSSARY_JSON = APPLICATION_ZANATA_GLOSSARY + XML;

   /**
    * Creates a format specific MediaType string given an existing media type
    * 
    * @param type the new type
    * @param from an existing media type with a format modifier such as xml or
    *           json
    * @return type with the format modifier from from
    */
   public static String createFormatSpecificType(String type, MediaType from)
   {
      StringBuilder str = new StringBuilder(type);
      String subtype = from.getSubtype();
      int plusIndex = subtype.indexOf('+');

      if (!(type.charAt(type.length() - 1) == '/'))
      {
         str.append('+');
      }

      if (plusIndex != -1)
         str.append(subtype.substring(plusIndex + 1));
      else
         str.append(subtype);

      return str.toString();
   }

}
