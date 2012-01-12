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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.zanata.webtrans.shared.validation.ValidationUtils;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class HtmlXmlTagValidation extends ValidationAction
{
   public HtmlXmlTagValidation(String id, String description)
   {
      super(id, description);
   }

   // private final static String tagRegex = "<[^>]+>[^<]*</[^>]+>";
   private final static String tagRegex = "<[^>]+>";

   private final static RegExp regExp = RegExp.compile(tagRegex, "g");

   @Override
   public void validate(String source, String target)
   {
      if (!ValidationUtils.isEmpty(target))
      {
         String error = runValidation(source, target);
         if (error.length() > 0)
         {
            addError("Tag [" + error + "] missing in target");
         }

         error = runValidation(target, source);
         if (error.length() > 0)
         {
            addError("Tag [" + error + "] missing in source");
         }

         if (getError().isEmpty())
         {
            orderValidation(source, target);
         }
      }
   }

   private void orderValidation(String source, String target)
   {
      List<String> from = getTagList(source);
      List<String> to = getTagList(target);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < from.size(); i++)
      {
         if (!to.get(i).equals(from.get(i)))
         {
            sb.append(" ");
            sb.append(from.get(i));
            sb.append(" ");
         }
      }

      if (sb.length() > 0)
      {
         addError("Tag [" + sb.toString() + "] are wrong in order");
      }

   }

   private List<String> getTagList(String src)
   {
      List<String> list = new ArrayList<String>();
      MatchResult result = regExp.exec(src);
      while (result != null)
      {
         String node = result.getGroup(0);
         list.add(node);
         result = regExp.exec(src);
      }
      return list;
   }

   private String runValidation(String compareFrom, String compareTo)
   {
      String tmp = compareTo;
      StringBuilder sb = new StringBuilder();
      MatchResult result = regExp.exec(compareFrom);

      while (result != null)
      {
         String node = result.getGroup(0);
         Log.debug("Found Node:" + node);
         if (!tmp.contains(node))
         {
            sb.append(" ");
            sb.append(node);
            sb.append(" ");
         }
         else
         {
            tmp = tmp.replaceFirst(node, ""); // remove matched node from
         }
         result = regExp.exec(compareFrom);
      }
      return sb.toString();
   }
}
