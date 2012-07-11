/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
import java.util.HashMap;
import java.util.Map.Entry;

import org.zanata.webtrans.client.resources.ValidationMessages;

/**
 * Checks for consistent java-style variables between two strings.
 * 
 * The current implementation will only check that each argument index is used a
 * consistent number of times. This will be extended in future to check that
 * each argument index is used with the same FormatType.
 * 
 * @author David Mason, damason@redhat.com
 * @see http://docs.oracle.com/javase/1.4.2/docs/api/java/text/MessageFormat.html
 **/
public class JavaVariablesValidation extends AbstractValidation
{
   public JavaVariablesValidation(final ValidationMessages messages)
   {
      super(messages.javaVariablesValidatorName(), messages.javaVariablesValidatorDescription(), true, messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceVars = findJavaVars(source);
      ArrayList<String> targetVars = findJavaVars(target);

      //check if any indices are added/missing
      HashMap<String, Integer> sourceCounts = countIndices(sourceVars);
      HashMap<String, Integer> targetCounts = countIndices(targetVars);
      ArrayList<String> missing = new ArrayList<String>();
      ArrayList<String> added = new ArrayList<String>();
      ArrayList<String> different = new ArrayList<String>();

      for (Entry<String, Integer> sourceVar : sourceCounts.entrySet())
      {
         Integer targetCount = targetCounts.remove(sourceVar.getKey());
         if (targetCount == null)
         {
            missing.add("{" + sourceVar.getKey() + "}");
         }
         else if (!sourceVar.getValue().equals(targetCount))
         {
            different.add("{" + sourceVar.getKey() + "}");
         }
      }
      for (String targetVar : targetCounts.keySet())
      {
         added.add("{" + targetVar + "}");
      }

      if (!missing.isEmpty())
      {
         addError(getMessages().varsMissing(missing));
      }
      if (!added.isEmpty())
      {
         addError(getMessages().varsAdded(added));
      }
      if (!different.isEmpty())
      {
         addError(getMessages().differentVarCount(different));
      }

      //TODO check if indices are used with the same format types
      //e.g. "You owe me {0, currency}" --> "Du schuldest mir {0, percent}" is not correct
   }

   private HashMap<String, Integer> countIndices(ArrayList<String> fullVars)
   {
      HashMap<String, Integer> argumentIndexCounts = new HashMap<String, Integer>();
      for (String fullVar : fullVars)
      {
         int argIndexEnd = fullVar.indexOf(',');
         argIndexEnd = (argIndexEnd != -1 ? argIndexEnd : fullVar.length() - 1);
         String argumentIndex = fullVar.substring(1, argIndexEnd).trim();

         if (argumentIndexCounts.containsKey(argumentIndex))
            argumentIndexCounts.put(argumentIndex, argumentIndexCounts.get(argumentIndex) + 1);
         else
            argumentIndexCounts.put(argumentIndex, 1);
      }
      return argumentIndexCounts;
   }

   private ArrayList<String> findJavaVars(String inString)
   {
      ArrayList<String> vars = new ArrayList<String>();
      //stack of opening brace positions, replace if better gwt LIFO collection found
      ArrayList<Integer> openings = new ArrayList<Integer>();
      ArrayList<Character> escapeChars = new ArrayList<Character>();
      escapeChars.add('\\');
      boolean isEscaped = false;
      boolean isQuoted = false;
      //scan for opening brace
      for (int i = 0; i<inString.length(); i++)
      {
         if (isEscaped)
         {
            isEscaped = false;
            continue;
         }

         //TODO add handling of quoting within SubFormatPatternParts and Strings

         char c = inString.charAt(i);

         if (c == '\'')
         {
            isQuoted = !isQuoted;
            continue;
         }
         if (isQuoted)
         {
            continue;
         }
         if (escapeChars.contains(c))
         {
            isEscaped = true;
            continue;
         }
         if (c == '{')
         {
            openings.add(i);
         }
         else if (c == '}' && openings.size() > 0)
         {
            String variable = inString.substring(openings.remove(openings.size() -1), i + 1);
            vars.add(variable);
         }
      }
      return vars;
   }
}
