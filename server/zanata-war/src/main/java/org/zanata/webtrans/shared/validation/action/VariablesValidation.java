/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.shared.validation.action;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class VariablesValidation extends ValidationAction
{
   public VariablesValidation(String id, String description)
   {
      super(id, description);
   }

   private final String[] varList = { "%s", "%d" };
   
   @Override
   public void validate(String source, String target)
   {
      for (String var : varList)
      {
         int srcCount = countMatches(source, var);
         int tgtCount = countMatches(target, var);
         Log.debug("Variable [" + var + "]: src-" + srcCount + " target-" + tgtCount);
         if (srcCount != tgtCount)
         {
            addError("Variable [" + var + "] count mismatch");
         }
      }
   }

   private int countMatches(String str, String sub)
   {
      if ((str == null || str.length() == 0) || (sub == null || sub.length() == 0))
      {
         return 0;
      }
      int count = 0;
      int idx = 0;
      while ((idx = str.indexOf(sub, idx)) != -1)
      {
         count++;
         idx += sub.length();
      }
      return count;
   }
}
