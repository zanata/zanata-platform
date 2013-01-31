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
package org.zanata.webtrans.shared.validation;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.base.Splitter;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class XmlEntityValidation extends AbstractValidation
{
   // &amp;, &quot;
   private final static String charRefRegex = "&[:a-z_A-Z][a-z_A-Z0-9.-]*;";
   private final static RegExp charRefExp = RegExp.compile(charRefRegex);

   // &#[numeric]
   private final static String decimalRefRegex = ".*&#[0-9]+;";
   private final static RegExp decimalRefExp = RegExp.compile(decimalRefRegex);

   // &#x[hexadecimal]
   private final static String hexadecimalRefRegex = ".*&#x[0-9a-f_A-F]+;";
   private final static RegExp hexadecimalRefExp = RegExp.compile(hexadecimalRefRegex);

   private final static String ENTITY_START_CHAR = "&";

   public XmlEntityValidation(ValidationId id, String desc, boolean enabled, ValidationMessages messages)
   {
      super(id, desc, enabled, messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      validateIncompleteEntity(target);
   }

   private void validateIncompleteEntity(String target)
   {
      Iterable<String> words = Splitter.on(" ").trimResults().omitEmptyStrings().split(target);

      for (String word : words)
      {
         if (word.contains(ENTITY_START_CHAR) && word.length() > 1)
         {
            word = replaceEntityWithEmptyString(charRefExp, word);
            word = replaceEntityWithEmptyString(decimalRefExp, word);
            word = replaceEntityWithEmptyString(hexadecimalRefExp, word);

            if (word.contains(ENTITY_START_CHAR))
            {
               //remove any string that occurs in front
               word = word.substring(word.indexOf(ENTITY_START_CHAR)); 
               addError(getMessages().invalidXMLEntity(word));
            }
         }
      }
   }

   /**
    * Replace matched string with empty string
    * 
    * @param regex
    * @param text
    * @return
    */
   private static String replaceEntityWithEmptyString(RegExp regex, String text)
   {
      MatchResult result = regex.exec(text);
      while (result != null)
      {
         // replace match entity with empty string
         text = text.replace(result.getGroup(0), ""); 
         result = regex.exec(text);
      }
      return text;
   }
}
