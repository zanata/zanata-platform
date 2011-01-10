/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
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
