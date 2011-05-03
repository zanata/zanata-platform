package org.zanata.exception;

public class ZanataServiceException extends ZanataException
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ZanataServiceException(String message, Throwable e)
   {
      super(message, e);
   }

   public ZanataServiceException(String message)
   {
      super(message);
   }

}
