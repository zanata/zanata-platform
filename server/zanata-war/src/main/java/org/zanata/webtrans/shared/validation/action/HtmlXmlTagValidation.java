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

import org.zanata.webtrans.client.resources.ValidationMessages;
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
   public HtmlXmlTagValidation(final ValidationMessages messages)
   {
      super(messages.xmlHtmlValidatorName(), messages.xmlHtmlValidatorDescription(), messages);
   }

   // private final static String tagRegex = "<[^>]+>[^<]*</[^>]+>";
   private final static String tagRegex = "<[^>]+>";

   private final static RegExp regExp = RegExp.compile(tagRegex, "g");

   @Override
   public void validate(String source, String target)
   {
      if (!ValidationUtils.isEmpty(target))
      {
         List<String> error = runValidation(source, target);
         if (!error.isEmpty())
         {

            addError(getMessages().tagsMissing(error));
         }

         error = runValidation(target, source);
         if (!error.isEmpty())
         {
            addError(getMessages().tagsAdded(error));
         }

         if (getError().isEmpty())
         {
            orderValidation(source, target);
         }
      }
   }

   private void orderValidation(String source, String target)
   {
      // TODO improve for cases such as first node moved to end and last node
      // moved to start. Currently reports every node in these cases, should
      // only report the one moved node.
      List<String> from = getTagList(source);
      List<String> to = getTagList(target);
      List<String> outOfOrder = new ArrayList<String>();

      for (int i = 0; i < from.size(); i++)
      {
         if (!to.get(i).equals(from.get(i)))
         {
            outOfOrder.add(from.get(i));
         }
      }

      if (!outOfOrder.isEmpty())
      {
         addError(getMessages().tagsWrongOrder(outOfOrder));
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

   private List<String> runValidation(String compareFrom, String compareTo)
   {
      String tmp = compareTo;
      List<String> unmatched = new ArrayList<String>();
      MatchResult result = regExp.exec(compareFrom);

      while (result != null)
      {
         String node = result.getGroup(0);
         Log.debug("Found Node:" + node);
         if (!tmp.contains(node))
         {
            unmatched.add(node);
         }
         else
         {
            tmp = tmp.replaceFirst(node, ""); // remove matched node from
         }
         result = regExp.exec(compareFrom);
      }
      return unmatched;
   }
}
