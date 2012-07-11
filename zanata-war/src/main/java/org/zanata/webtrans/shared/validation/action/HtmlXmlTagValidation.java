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

import org.zanata.webtrans.client.resources.ValidationMessages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class HtmlXmlTagValidation extends AbstractValidation
{
   public HtmlXmlTagValidation(final ValidationMessages messages)
   {
      super(messages.xmlHtmlValidatorName(), messages.xmlHtmlValidatorDescription(), true, messages);
   }

   // private final static String tagRegex = "<[^>]+>[^<]*</[^>]+>";
   private final static String tagRegex = "<[^>]+>";

   private final static RegExp regExp = RegExp.compile(tagRegex, "g");

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceTags = getTagList(source);
      ArrayList<String> targetTags = getTagList(target);

      ArrayList<String> error = listMissing(source, target);
      if (!error.isEmpty())
      {
         addError(getMessages().tagsMissing(error));
      }

      boolean noError = error.isEmpty();

      error = listMissing(target, source);
      if (!error.isEmpty())
      {
         addError(getMessages().tagsAdded(error));
      }

      noError &= error.isEmpty();

      if (noError)
      {
         orderValidation(sourceTags, targetTags);
      }
   }

   private void orderValidation(ArrayList<String> srcTags, ArrayList<String> trgTags)
   {
      ArrayList<String> longestRun = null;
      ArrayList<String> currentRun;

      String[] src = srcTags.toArray(new String[srcTags.size()]);
      String[] trg = trgTags.toArray(new String[trgTags.size()]);

      for (int i = 0; i < src.length; i++)
      {
         String token = src[i];
         int srcIndex = i;
         int trgIndex = trgTags.indexOf(token);

         if (trgIndex > -1)
         {
            currentRun = new ArrayList<String>();
            currentRun.add(token);

            int j = trgIndex + 1;

            while (j < trg.length && srcIndex < src.length - 1)
            {
               int nextIndexInSrc = findInTail(trg[j], src, srcIndex + 1);
               if (nextIndexInSrc > -1)
               {
                  srcIndex = nextIndexInSrc;
                  currentRun.add(src[srcIndex]);
               }
               j++;
            }

            if (currentRun.size() == srcTags.size())
            {
               // must all match
               return;
            }

            if (longestRun == null || longestRun.size() < currentRun.size())
            {
               longestRun = currentRun;
            }
         }
      }

      if (longestRun != null && longestRun.size() > 0)
      {
         ArrayList<String> outOfOrder = new ArrayList<String>();

         for (String aSrc : src)
         {
            if (!longestRun.contains(aSrc))
            {
               outOfOrder.add(aSrc);
            }
         }

         addError(getMessages().tagsWrongOrder(outOfOrder));
      }
   }

   private int findInTail(String toFind, String[] findIn, int startIndex)
   {
      for (int i = startIndex; i < findIn.length; i++)
      {
         if (findIn[i].equals(toFind))
         {
            return i;
         }
      }
      return -1;
   }

   private ArrayList<String> getTagList(String src)
   {
      ArrayList<String> list = new ArrayList<String>();
      MatchResult result = regExp.exec(src);
      while (result != null)
      {
         String node = result.getGroup(0);
         list.add(node);
         result = regExp.exec(src);
      }
      return list;
   }

   private ArrayList<String> listMissing(String compareFrom, String compareTo)
   {
      String tmp = compareTo;
      ArrayList<String> unmatched = new ArrayList<String>();
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
