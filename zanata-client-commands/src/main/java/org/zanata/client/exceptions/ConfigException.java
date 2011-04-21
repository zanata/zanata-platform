package org.zanata.client.exceptions;

public class ConfigException extends RuntimeException
{

   private static final long serialVersionUID = 1L;

   public ConfigException()
   {
      super();
   }

   public ConfigException(String message)
   {
      super(message);
   }

   public ConfigException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ConfigException(Throwable cause)
   {
      super(cause);
   }

}
