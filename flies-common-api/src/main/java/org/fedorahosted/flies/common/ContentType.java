package org.fedorahosted.flies.common;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public final class ContentType implements Serializable
{

   private static final long serialVersionUID = -7977805381672178179L;

   private final String contentType;
   // TODO split up

   public static final ContentType TextPlain = new ContentType("text/plain");
   public static final ContentType PO = new ContentType("application/x-gettext");

   // JaxB needs a no-arg constructor :(
   @SuppressWarnings("unused")
   private ContentType()
   {
      contentType = null;
   }

   @JsonCreator
   public ContentType(String contentType)
   {
      if (contentType == null)
         throw new IllegalArgumentException("localeId");
      this.contentType = contentType.intern();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (!(obj instanceof ContentType))
         return false;
      return this.contentType == ((ContentType) obj).contentType;
   }

   @Override
   public int hashCode()
   {
      return contentType.hashCode();
   }

   @Override
   @JsonValue
   public String toString()
   {
      return contentType;
   }

}
