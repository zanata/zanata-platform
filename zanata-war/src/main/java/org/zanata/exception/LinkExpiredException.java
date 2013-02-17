package org.zanata.exception;

public class LinkExpiredException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public LinkExpiredException()
   {
      super();
   }

   public LinkExpiredException(String message)
   {
      super(message);
   }

}
