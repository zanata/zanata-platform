package net.openl10n.flies.rest.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.openl10n.flies.common.ContentType;

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