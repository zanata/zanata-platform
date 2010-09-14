package net.openl10n.flies.exception;

import javax.ws.rs.core.Response.Status;

public class FliesRestException extends FliesException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final Status returnCode;

   public FliesRestException(Status returnCode, String message)
   {
      this(returnCode, message, null);
   }

   public FliesRestException(Status returnCode, String message, Throwable e)
   {
      super(message, e);
      this.returnCode = returnCode;
   }

   public FliesRestException(String message)
   {
      this(null, message, null);
   }

   public Status httpReturnCode()
   {
      return returnCode;
   }

}
