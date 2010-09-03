package net.openl10n.flies.exception;

import javax.ws.rs.core.Response.Status;

public class FliesException extends RuntimeException
{
   private static final long serialVersionUID = 1L;
   private final Status returnCode;

   private final String message;

   public FliesException(Status returnCode, String message)
   {
      this(returnCode, message, null);
   }

   public FliesException(Status returnCode, String message, Throwable e)
   {
      super(message, e);
      this.returnCode = returnCode;
      this.message = message;
   }

   public FliesException(String message)
   {
      this(null, message, null);
   }

   public String message()
   {
      return message;
   }

   public Status httpReturnCode()
   {
      return returnCode;
   }
}
