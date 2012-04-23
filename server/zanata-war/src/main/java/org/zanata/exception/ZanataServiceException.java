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

   /**
    * Service exception that indicates an expected entity was not found by the service.
    * Roughly maps to a Not Found http response code.
    */
   public static class EntityNotFoundException extends ZanataException
   {
      public EntityNotFoundException(String message, Throwable e)
      {
         super(message, e);
      }

      public EntityNotFoundException(String message)
      {
         super(message);
      }
   }

   /**
    * Service exception that indicates a problem with the parameters passed to a service.
    * Roughly maps to a Bad Request http response code.
    */
   public static class InvalidParameterException extends ZanataException
   {
      public InvalidParameterException(String message, Throwable e)
      {
         super(message, e);
      }

      public InvalidParameterException(String message)
      {
         super(message);
      }
   }

   /**
    * Service exception that indicates that the user does not have access to a specific entity or resource.
    * Roughly maps to a Forbidden http response code.
    */
   public static class AccessDeniedException extends ZanataException
   {
      public AccessDeniedException(String message, Throwable e)
      {
         super(message, e);
      }

      public AccessDeniedException(String message)
      {
         super(message);
      }
   }

}
