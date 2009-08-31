package org.fedorahosted.flies;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ContentTypeAdapter extends XmlAdapter<String, ContentType>
{
   public ContentType unmarshal(String s) throws Exception
   {
      if (s == null) return null;
      return new ContentType(s);
   }

   public String marshal(ContentType contentType) throws Exception
   {
      if (contentType == null) return null;
      return contentType.toString();
   }
   
}