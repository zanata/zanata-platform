package org.zanata.security;

public class KeyNotFoundException extends RuntimeException
{
   private static final long serialVersionUID = -792069428655695057L;

   public KeyNotFoundException()
   {
      super();
   }

   public KeyNotFoundException(String message)
   {
      super(message);
   }

}
