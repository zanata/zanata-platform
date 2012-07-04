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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class PrintfVariablesValidation extends ValidationAction
{

   private static final String GLOBAL_FLAG = "g";

   // derived from translate toolkit printf style variable matching regex. See:
   // http://translate.svn.sourceforge.net/viewvc/translate/src/trunk/translate/filters/checks.py?revision=17978&view=markup
   private static final String VAR_REGEX = "%((?:\\d+\\$|\\(\\w+\\))?[+#-]*(\\d+)?(\\.\\d+)?(hh|h|ll|l|L|z|j|t)?[\\w%])";

   // regex to find out whether the variable has position
   private static final RegExp POSITIONAL_REG_EXP = RegExp.compile("%(\\d+\\$)\\w+");


   public PrintfVariablesValidation(final ValidationMessages messages)
   {
      super(messages.printfVariablesValidatorName(), messages.printfVariablesValidatorDescription(), true, messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceVars = findVars(source);
      ArrayList<String> targetVars = findVars(target);

      Log.debug("source vars: " + sourceVars);
      Log.debug("target vars: " + targetVars);

      ArrayList<String> targetAfterStrip = stripPositionIfApplicable(targetVars);
      Log.debug("target after stripping position: " + targetAfterStrip);

      List<String> missing = listMissing(sourceVars, targetAfterStrip);
      if (!missing.isEmpty())
      {
         addError(getMessages().varsMissing(missing));
      }

      // missing from source = added
      missing = listMissing(targetAfterStrip, sourceVars);
      if (!missing.isEmpty())
      {
         addError(getMessages().varsAdded(missing));
      }
   }

   private List<String> listMissing(ArrayList<String> baseVars, ArrayList<String> testVars)
   {
      ArrayList<String> remainingVars = new ArrayList<String>(testVars);

      ArrayList<String> unmatched = new ArrayList<String>();

      for (String var : baseVars)
      {
         if (!remainingVars.remove(var))
         {
            unmatched.add(var);
         }
      }
      return unmatched;
   }

   private static ArrayList<String> stripPositionIfApplicable(ArrayList<String> variables)
   {
      ArrayList<String> copyOfVariables = new ArrayList<String>(variables);

      boolean hasPosition = false;
      for (String testVar : variables)
      {
         MatchResult result = POSITIONAL_REG_EXP.exec(testVar);
         if (result != null)
         {
            hasPosition = true;
            String positionAndDollar = result.getGroup(1);
            int position = extractPositionIndex(positionAndDollar);
            if (position >= 0 && position < variables.size())
            {
               copyOfVariables.set(position, testVar.replace(positionAndDollar, ""));
            }
            else
            {
               Log.info("positional variable has invalid position. Ignore position strip.");
               return variables;
            }
         }
         else if (hasPosition)
         {
            Log.info("encounter mixed positional and unpositional variables. Ignore position strip.");
            return variables;
         }
      }
      return copyOfVariables;
   }

   private static int extractPositionIndex(String positionAndDollar)
   {
      try
      {
         return Integer.valueOf(positionAndDollar.substring(0, positionAndDollar.length() - 1)) - 1;
      }
      catch (Exception e)
      {
         Log.info("cannot extract position index from " + positionAndDollar);
         return -1;
      }
   }

   private ArrayList<String> findVars(String inString)
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
