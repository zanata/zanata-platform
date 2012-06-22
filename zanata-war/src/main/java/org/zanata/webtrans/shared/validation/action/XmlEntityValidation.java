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

import org.zanata.webtrans.client.resources.ValidationMessages;

import com.google.common.base.Splitter;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.thirdparty.guava.common.base.Strings;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class XmlEntityValidation extends ValidationAction
{
   
   private final static String entityRegex = "&[:a-z_A-Z][a-z_A-Z0-9.-]*;";
   private final static RegExp entityExp = RegExp.compile(entityRegex);
   
   
   private final static String entityNameRegex = "[:a-z_A-Z][a-z_A-Z0-9.-]*";
   private final static RegExp entityNameExp = RegExp.compile(entityNameRegex);
   
   private final static String ENTITY_START_CHAR= "&";
   private final static String ENTITY_END_CHAR= ";";
   
   public XmlEntityValidation(final ValidationMessages messages)
   {
      super(messages.xmlEntityValidatorName(), messages.xmlEntityValidatorDescription(), true, messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      if(Strings.isNullOrEmpty(target))
      {
         return;
      }
      
      Iterable<String> words = Splitter.on(" ").trimResults().omitEmptyStrings().split(target);
      
      for(String word: words)
      {
         if(word.startsWith(ENTITY_START_CHAR) && word.endsWith(ENTITY_END_CHAR))
         {
            if(!entityExp.test(word))
            {
               addError("Possible XML entity '" + word + "' does not match naming requirements");
            }
         } 
         else if(word.startsWith(ENTITY_START_CHAR))
         {
            if(entityNameExp.test(word.substring(1)))
            {
               addError("Possible XML entity '" + word + "' does not end with '" + ENTITY_END_CHAR + "'");
            }
         }
         else if(word.endsWith(ENTITY_END_CHAR))
         {
            if(entityNameExp.test(word.substring(0, word.length() - 1)))
            {
               addError("Possible XML entity '" + word + "' does not starts with '" + ENTITY_START_CHAR + "'");
            }
         }
         else 
         {
            //normal wording, ignore
         }
      }
   }
}
