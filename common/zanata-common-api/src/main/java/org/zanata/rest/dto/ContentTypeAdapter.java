package org.zanata.rest.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.zanata.common.ContentType;


public class ContentTypeAdapter extends XmlAdapter<String, ContentType>
{
   public ContentType unmarshal(String s) throws Exception
   {
      if (s == null)
         return null;
      return new ContentType(s);
   }

   public String marshal(ContentType contentType) throws Exception
   {
      if (contentType == null)
         return null;
      return contentType.toString();
   }

}