package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "projectTypeType")
@XmlEnum(String.class)
public enum ProjectType
{
   Utf8Properties, Properties, Gettext, Podir, Xliff, Xml, File;

   private static final String OBSOLETE_PROJECT_TYPE_RAW = "raw";

   /**
    * @param projectType
    * @return
    * @throws Exception
    */
   public static ProjectType getValueOf(String projectType) throws Exception
   {
      if (projectType != null && !projectType.isEmpty())
      {
         for (ProjectType pt : ProjectType.values())
         {
            if (pt.name().equalsIgnoreCase(projectType))
            {
               return pt;
            }
         }
      }
      if (OBSOLETE_PROJECT_TYPE_RAW.equalsIgnoreCase(projectType))
      {
         throw new Exception("Project type '" + projectType + "' no longer supported, use 'File' instead");
      }
      throw new Exception("Project type '" + projectType + "' not supported");
   }
}