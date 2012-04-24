package org.zanata.exception;


abstract class ZanataException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public ZanataException(String message, Throwable e)
   {
      super(message, e);
   }

   public ZanataException(String message)
   {
      super(message);
   }

}
