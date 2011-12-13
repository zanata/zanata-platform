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
      Log.info("Source:" + source);
      Log.info("target:" + target);

      MatchResult result = regExp.exec(source);
      while (result != null)
      {
         String node = result.getGroup(0);
         Log.info("Found Node:" + node);
         if (!target.contains(node))
         {
            addError(getId() + ":" + node + " not found in target");
         }
         result = regExp.exec(source);
      }
   }
}
