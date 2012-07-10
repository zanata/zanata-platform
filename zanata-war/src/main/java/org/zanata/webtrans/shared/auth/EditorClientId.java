package org.zanata.webtrans.shared.auth;

import org.zanata.webtrans.shared.model.Identifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a single connection to Zanata from GWT.  Usually represents one tab in a browser.  It has a one-to-one relationship with
 * GWT Event Service's client ID / connection ID, but it is not the same value.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public final class EditorClientId implements Identifier<String>, IsSerializable
{
   private String httpSessionId;
   private long editorClientNum;

   @SuppressWarnings("unused")
   private EditorClientId()
   {
   }

   public EditorClientId(String httpSessionId, long editorClientNum)
   {
      if (httpSessionId == null || httpSessionId.isEmpty())
      {
         throw new IllegalStateException("Invalid Id");
      }
      this.httpSessionId = httpSessionId;
      this.editorClientNum = editorClientNum;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof EditorClientId))
      {
         return false;
      }
      EditorClientId other = (EditorClientId) obj;
      if (editorClientNum != other.editorClientNum)
      {
         return false;
      }
      if (httpSessionId == null)
      {
         if (other.httpSessionId != null)
         {
            return false;
         }
      }
      else if (!httpSessionId.equals(other.httpSessionId))
      {
         return false;
      }
      return true;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (editorClientNum ^ (editorClientNum >>> 32));
      result = prime * result + ((httpSessionId == null) ? 0 : httpSessionId.hashCode());
      return result;
   }
   
   public String getHttpSessionId()
   {
      return httpSessionId;
   }

   @Override
   public String toString()
   {
      return getValue();
   }

   @Override
   public String getValue()
   {
      return httpSessionId + ":" + editorClientNum;
   }

}
