package net.openl10n.flies.exception;


abstract class FliesException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public FliesException(String message, Throwable e)
   {
      super(message, e);
   }

   public FliesException(String message)
   {
      super(message);
   }

}
