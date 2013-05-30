package java.net;

import java.io.Serializable;
import java.lang.Comparable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.UnsupportedOperationException;

/**
 * Simplified version of JRE's class for use in GWT.
 */
public class URI implements Serializable, Comparable<URI>
{
   private String uriString;

   // for GWT use
   private URI()
   {
   }

   public URI(String uriString)
   {
      this.uriString = uriString;
   }

   @Override
   public int compareTo(URI o)
   {
      return uriString.compareTo(o.uriString);
   }

   @Override
   public int hashCode()
   {
      return uriString.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      return uriString.equals(((URI) obj).toString());
   }

   @Override
   public String toString()
   {
      return uriString;
   }
}