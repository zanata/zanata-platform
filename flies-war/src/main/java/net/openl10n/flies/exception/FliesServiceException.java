package net.openl10n.flies.exception;

public class FliesServiceException extends FliesException
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public FliesServiceException(String message, Throwable e)
   {
      super(message, e);
   }

   public FliesServiceException(String message)
   {
      super(message);
   }

}
