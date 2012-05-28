package org.zanata.webtrans.shared.auth;

import java.io.Serializable;

import org.zanata.webtrans.shared.model.Identifier;

/**
 * Represents a single connection to Zanata from GWT.  Usually represents one tab in a browser.  It has a one-to-one relationship with
 * GWT Event Service's client ID / connection ID, but it is not the same value.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public final class EditorClientId implements Identifier<String>, Serializable
{
   // generated
   private static final long serialVersionUID = 6713691712353126602L;

   private String id;

   @SuppressWarnings("unused")
   private EditorClientId()
   {
   }

   public EditorClientId(String id)
   {
      if (id == null || id.isEmpty())
      {
         throw new IllegalStateException("Invalid Id");
      }
      this.id = id;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (obj instanceof EditorClientId)
      {
         return ((EditorClientId) obj).id.equals(id);
      }
      return super.equals(obj);
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }

   @Override
   public String toString()
   {
      return id;
   }

   @Override
   public String getValue()
   {
      return id;
   }

}
