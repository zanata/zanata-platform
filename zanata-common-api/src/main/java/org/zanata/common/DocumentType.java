package org.zanata.common;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public enum DocumentType
{
   GETTEXT_PORTABLE_OBJECT("po"),
   GETTEXT_PORTABLE_OBJECT_TEMPLATE("pot"),
   PLAIN_TEXT("txt"),
   XML_DOCUMENT_TYPE_DEFINITION("dtd"),

   OPEN_DOCUMENT_TEXT("odt"),
   OPEN_DOCUMENT_TEXT_FLAT("fodt"),
   OPEN_DOCUMENT_PRESENTATION("odp"),
   OPEN_DOCUMENT_PRESENTATION_FLAT("fodp"),
   OPEN_DOCUMENT_SPREADSHEET("ods"),
   OPEN_DOCUMENT_SPREADSHEET_FLAT("fods"),
   OPEN_DOCUMENT_GRAPHICS("odg"),
   OPEN_DOCUMENT_GRAPHICS_FLAT("fodg"),
   OPEN_DOCUMENT_DATABASE("odb"),
   OPEN_DOCUMENT_FORMULA("odf"),

   IDML("idml");

   private static final List<String> allExtensions = buildExtensionsList();

   private static List<String> buildExtensionsList()
   {
      List<String> allExtensions = new ArrayList<String>();
      for (DocumentType type : DocumentType.values())
      {
         allExtensions.add(type.getExtension());
      }
      return unmodifiableList(allExtensions);
   }

   /**
    * 
    * @return a read-only list of file extensions for known file types
    */
   public static List<String> getAllExtensions()
   {
      return allExtensions;
   }

   // FIXME damason: rename typeFor to fromString
   public static DocumentType typeFor(String extension)
   {
      for (DocumentType type : DocumentType.values())
      {
         if (type.getExtension().equals(extension))
         {
            return type;
         }
      }
      // FIXME damason: throw new IllegalArgumentException
      return null;
   }

   private final String extension;

   DocumentType(String extension)
   {
      this.extension = extension;
   }

   // FIXME damason: rename getExtension to toString
   public String getExtension()
   {
      return extension;
   }

}
