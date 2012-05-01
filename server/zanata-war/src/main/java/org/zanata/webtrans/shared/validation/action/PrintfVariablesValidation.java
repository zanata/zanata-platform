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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class PrintfVariablesValidation extends ValidationAction
{
   public PrintfVariablesValidation(final ValidationMessages messages)
   {
      super(messages.printfVariablesValidatorName(), messages.printfVariablesValidatorDescription(), true, messages);
   }


   // derived from translate toolkit printf style variable matching regex. See:
   // http://translate.svn.sourceforge.net/viewvc/translate/src/trunk/translate/filters/checks.py?revision=17978&view=markup
   private final static String varRegex = "%((?:\\d+\\$|\\(\\w+\\))?[+#-]*(\\d+)?(\\.\\d+)?(hh|h|ll|l|L|z|j|t)?[\\w%])";
   // private final static String varRegex = "%[\\w]+";

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceVars = findVars(source);
      ArrayList<String> targetVars = findVars(target);

      List<String> missing = listMissing(sourceVars, targetVars);
      if (!missing.isEmpty())
      {
         addError(getMessages().varsMissing(missing));
      }

      // missing from source = added
      missing = listMissing(targetVars, sourceVars);
      if (!missing.isEmpty())
      {
         addError(getMessages().varsAdded(missing));
      }
   }

   private List<String> listMissing(ArrayList<String> baseVars, ArrayList<String> testVars)
   {
      ArrayList<String> remainingVars = new ArrayList<String>();
      remainingVars.addAll(testVars);
      ArrayList<String> unmatched = new ArrayList<String>();

      for (String var : baseVars)
         if (!remainingVars.remove(var))
            unmatched.add(var);
      return unmatched;
   }

   private ArrayList<String> findVars(String inString)
   {
      ArrayList<String> vars = new ArrayList<String>();
      // compile each time to reset index
      RegExp varRegExp = RegExp.compile(varRegex, "g");
      MatchResult result = varRegExp.exec(inString);
      while (result != null)
      {
         vars.add(result.getGroup(0));
         result = varRegExp.exec(inString);
      }
      return vars;
   }
}
