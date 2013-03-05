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
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.AbstractValidationAction;

import com.google.common.collect.Lists;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class PrintfVariablesValidation extends AbstractValidationAction
{
   private static final String GLOBAL_FLAG = "g";

   // derived from translate toolkit printf style variable matching regex. See:
   // http://translate.svn.sourceforge.net/viewvc/translate/src/trunk/translate/filters/checks.py?revision=17978&view=markup
   private static final String VAR_REGEX = "%((?:\\d+\\$|\\(\\w+\\))?[+#-]*(\\d+)?(\\.\\d+)?(hh|h|ll|l|L|z|j|t)?[\\w%])";

   public PrintfVariablesValidation(ValidationId id, ValidationMessages messages)
   {
      super(new ValidationInfo(id, null, true), messages);
   }
   
   public PrintfVariablesValidation(ValidationId id, ValidationMessages messages, boolean enabled)
   {
      super(new ValidationInfo(id, null, enabled), messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceVars = findVars(source);
      ArrayList<String> targetVars = findVars(target);

      findMissingVariables(sourceVars, targetVars);
      findAddedVariables(sourceVars, targetVars);
   }

   protected void findMissingVariables(ArrayList<String> sourceVars, ArrayList<String> targetVars)
   {
      List<String> missing = listMissing(sourceVars, targetVars);
      if (!missing.isEmpty())
      {
         addError(getMessages().varsMissing(missing));
      }
   }

   protected void findAddedVariables(ArrayList<String> sourceVars, ArrayList<String> targetVars)
   {
      // missing from source = added
      List<String> added = listMissing(targetVars, sourceVars);
      if (!added.isEmpty())
      {
         addError(getMessages().varsAdded(added));
      }
   }

   private List<String> listMissing(ArrayList<String> baseVars, ArrayList<String> testVars)
   {
      ArrayList<String> remainingVars = Lists.newArrayList(testVars);

      ArrayList<String> unmatched = Lists.newArrayList();

      for (String var : baseVars)
      {
         if (!remainingVars.remove(var))
         {
            unmatched.add(var);
         }
      }
      return unmatched;
   }

   protected ArrayList<String> findVars(String inString)
   {
      ArrayList<String> vars = new ArrayList<String>();
      // compile each time to reset index
      RegExp varRegExp = RegExp.compile(VAR_REGEX, GLOBAL_FLAG);
      MatchResult result = varRegExp.exec(inString);
      while (result != null)
      {
         vars.add(result.getGroup(0));
         result = varRegExp.exec(inString);
      }
      return vars;
   }
}
