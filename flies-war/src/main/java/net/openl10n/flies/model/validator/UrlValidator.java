package net.openl10n.flies.model.validator;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.hibernate.validator.Validator;

public class UrlValidator implements Validator<Url>, Serializable
{

   private boolean canEndInSlash;

   public void initialize(Url parameters)
   {
      this.canEndInSlash = parameters.canEndInSlash();
   }

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
