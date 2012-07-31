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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class XmlEntityValidation extends AbstractValidation
{

   private final static String entityRegex = "&#?[a-z_A-Z0-9.-]+;";
   private final static RegExp entityExp = RegExp.compile(entityRegex);

   private final static RegExp entityGlobalExp = RegExp.compile(entityRegex, "g");

   private final static String ENTITY_START_CHAR = "&";

   // XML PREDEFINED ENTITY
   // private final static String[] PRE_DEFINED_ENTITY = { "&quot;", "&amp;",
   // "&apos;", "&lt;", "&gt;" };

   public XmlEntityValidation(final ValidationMessages messages)
   {
      super(messages.xmlEntityValidatorName(), messages.xmlEntityValidatorDescription(), true, messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      validateIncompleteEntity(target);
      validateSourceTargetEntity(source, target);
   }

   private void validateSourceTargetEntity(String source, String target)
   {
      if (Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(target))
      {
         return;
      }

      String tmp = target;
      ArrayList<String> unmatched = new ArrayList<String>();
      MatchResult result = entityGlobalExp.exec(source);

      while (result != null)
      {
         String entity = result.getGroup(0);
         Log.debug("Found entity:" + entity);
         if (!tmp.contains(entity))
         {
            unmatched.add(" [" + entity + "] ");
         }
         else
         {
            tmp = tmp.replaceFirst(entity, ""); // remove matched entity from
         }
         result = entityGlobalExp.exec(source);
      }
      
      if (!unmatched.isEmpty())
      {
         addError(getMessages().entityMissing(unmatched));
      }

   }

   private void validateIncompleteEntity(String target)
   {
      if (Strings.isNullOrEmpty(target))
      {
         return;
      }

      Iterable<String> words = Splitter.on(" ").trimResults().omitEmptyStrings().split(target);

      for (String word : words)
      {
         if (word.startsWith(ENTITY_START_CHAR) && word.length() > 1)
         {
            if (!entityExp.test(word))
            {
               addError(getMessages().incompleteXMLEntity(word));
            }
         }
      }
   }
}
