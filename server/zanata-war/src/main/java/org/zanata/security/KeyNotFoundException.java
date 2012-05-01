package org.zanata.security;

public class KeyNotFoundException extends RuntimeException
{

   public KeyNotFoundException()
   {
      super();
   }

   public KeyNotFoundException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public KeyNotFoundException(String message)
   {
      super(message);
   }

   public KeyNotFoundException(Throwable cause)
   {
      super(cause);
   }

}
