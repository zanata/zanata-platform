package net.openl10n.flies.model.validator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;

import org.hibernate.validator.Validator;

public class UrlValidator<U extends Annotation> implements Validator<U>, Serializable
{
   private static final long serialVersionUID = 1L;

   private boolean canEndInSlash;

   @Override
   public void initialize(U u)
   {
      if (u instanceof Url || u instanceof UrlNoSlash)
         this.canEndInSlash = u instanceof Url;
      else
         throw new RuntimeException("UrlValidator: unknown annotation " + u);
   }

   @Override
   public boolean isValid(Object value)
   {
      if (value == null)
         return true;
      if (!(value instanceof String))
         return false;
      String string = (String) value;
      if (!canEndInSlash && string.endsWith("/"))
         return false;

      try
      {
         new URL(string);
         return true;
      }
      catch (MalformedURLException e)
      {
         return false;
      }
   }

}
