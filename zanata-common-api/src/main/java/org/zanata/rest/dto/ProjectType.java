package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "projectTypeType")
@XmlEnum(String.class)
public enum ProjectType
{
   utf8properties, properties, gettext, podir, xliff, xml, raw;
   
   public static ProjectType getValueOf(String type) throws Exception
   {
      try
      {
         return valueOf(type.toLowerCase());
      }
      catch(Exception e)
      {
         throw new Exception("Projec type '" + type + "' not supported");
      }
   }
}