package java.net;

import java.lang.RuntimeException;
import java.lang.Throwable;

public class URISyntaxException extends Exception
{
   public URISyntaxException()
   {
   }

   public URISyntaxException(String msg)
   {
      super(msg);
   }

   public URISyntaxException(Throwable throwable)
   {
      super(throwable);
   }

   public URISyntaxException(String msg, Throwable throwable)
   {
      super(msg, throwable);
   }
}