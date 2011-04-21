package org.zanata.exception;

import javax.ws.rs.core.Response.Status;

public class ZanataRestException extends ZanataException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final Status returnCode;

   public ZanataRestException(Status returnCode, String message)
   {
      this(returnCode, message, null);
   }

   public ZanataRestException(Status returnCode, String message, Throwable e)
   {
      super(message, e);
      this.returnCode = returnCode;
   }

   public ZanataRestException(String message)
   {
      this(null, message, null);
   }

   public Status httpReturnCode()
   {
      return returnCode;
   }

}
