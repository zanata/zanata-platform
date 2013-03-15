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
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationInfo;
import org.zanata.webtrans.shared.validation.AbstractValidationAction;

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
public class JavaVariablesValidation extends AbstractValidationAction
{
   public JavaVariablesValidation(ValidationId id, ValidationMessages messages)
   {
      super(id, messages.javaVariablesValidatorDesc(), new ValidationInfo(true), messages);
   }

   @Override
   public void doValidate(String source, String target)
   {
      StringInfo sourceInfo = analyseString(source);
      StringInfo targetInfo = analyseString(target);

      //check if any indices are added/missing
      ArrayList<String> missing = new ArrayList<String>();
      ArrayList<String> missingQuoted = new ArrayList<String>();
      ArrayList<String> added = new ArrayList<String>();
      ArrayList<String> addedQuoted = new ArrayList<String>();
      ArrayList<String> different = new ArrayList<String>();

      for (Entry<String, Integer> sourceVar : sourceInfo.varCounts.entrySet())
      {
         Integer targetCount = targetInfo.varCounts.remove(sourceVar.getKey());
         if (targetCount == null)
         {
            Integer quotedCount = targetInfo.quotedVarCounts.get(sourceVar.getKey());
            if (quotedCount != null && quotedCount > 0)
            {
               missingQuoted.add("{" + sourceVar.getKey() + "}");
            }
            else
            {
               missing.add("{" + sourceVar.getKey() + "}");
            }
         }
         else if (!sourceVar.getValue().equals(targetCount))
         {
            if (targetInfo.quotedVars.contains(sourceVar.getKey()))
            {
               missingQuoted.add("{" + sourceVar.getKey() + "}");
            }
            else
            {
               different.add("{" + sourceVar.getKey() + "}");
            }
         }
      }

      // TODO could warn if they were quoted in original
      for (String targetVar : targetInfo.varCounts.keySet())
      {
         if (sourceInfo.quotedVarCounts.containsKey(targetVar))
         {
            addedQuoted.add("{" + targetVar + "}");
         }
         else
         {
            added.add("{" + targetVar + "}");
         }
      }

      boolean looksLikeMessageFormatString = !sourceInfo.varCounts.isEmpty();

      if (!missing.isEmpty())
      {
         addError(getMessages().varsMissing(missing));
      }

      if (looksLikeMessageFormatString && sourceInfo.singleApostrophes != targetInfo.singleApostrophes)
      {
         // different number of apos.
         addError(getMessages().differentApostropheCount());
      }
      if (looksLikeMessageFormatString && sourceInfo.quotedChars == 0 && targetInfo.quotedChars > 0)
      {
         // quoted chars in target but not source
         addError(getMessages().quotedCharsAdded());
      }
      if (!missingQuoted.isEmpty())
      {
         addError(getMessages().varsMissingQuoted(missingQuoted));
      }
      if (!added.isEmpty())
      {
         addError(getMessages().varsAdded(added));
      }
      if (!addedQuoted.isEmpty())
      {
         addError(getMessages().varsAddedQuoted(addedQuoted));
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

   private StringInfo analyseString(String inString)
   {
      StringInfo descriptor = new StringInfo();

      //stack of opening brace positions, replace if better gwt LIFO collection found
      ArrayList<Integer> openings = new ArrayList<Integer>();
      ArrayList<Integer> quotedOpenings = new ArrayList<Integer>();

      ArrayList<Character> escapeChars = new ArrayList<Character>();
      escapeChars.add('\\');

      boolean isEscaped = false;
      boolean isQuoted = false;
      int quotedLength = 0;

      //scan for opening brace
      for (int i = 0; i<inString.length(); i++)
      {
         // escaping skips a single character
         if (isEscaped)
         {
            isEscaped = false;
            continue;
         }

         //TODO add handling of quoting within SubFormatPatternParts and Strings

         char c = inString.charAt(i);

         // begin or end quoted sections
         if (c == '\'')
         {
            if (isQuoted)
            {
               if (quotedLength == 0)
               {
                  // don't count doubled quotes
                  descriptor.singleApostrophes--;
               }
               isQuoted = false;
            }
            else
            {
               isQuoted = true;
               quotedLength = 0;
               quotedOpenings.clear();
               descriptor.singleApostrophes++;
            }
            continue;
         }

         if (isQuoted)
         {
            quotedLength++;
            descriptor.quotedChars++;

            // identify quoted variables (not valid variables, identified to warn user)
            if (c == '{')
            {
               quotedOpenings.add(i);
            }
            else if (c == '}' && quotedOpenings.size() > 0)
            {
               String variable = inString.substring(quotedOpenings.remove(quotedOpenings.size() -1), i + 1);
               descriptor.quotedVars.add(variable);
            }

            continue;
         }

         // identify escape character (intentionally after quoted section handling)
         if (escapeChars.contains(c))
         {
            isEscaped = true;
            continue;
         }

         // identify non-quoted variables
         if (c == '{')
         {
            openings.add(i);
         }
         else if (c == '}' && openings.size() > 0)
         {
            String variable = inString.substring(openings.remove(openings.size() -1), i + 1);
            descriptor.vars.add(variable);
         }
      }

      descriptor.varCounts = countIndices(descriptor.vars);
      descriptor.quotedVarCounts = countIndices(descriptor.quotedVars);

      return descriptor;
   }

   /**
    * Holds information about java variables, quoting etc. for a string.
    */
   private class StringInfo
   {
      private int quotedChars = 0;
      private int singleApostrophes = 0;

      private ArrayList<String> vars = new ArrayList<String>();
      private ArrayList<String> quotedVars = new ArrayList<String>();

      HashMap<String, Integer> varCounts;
      HashMap<String, Integer> quotedVarCounts;
   }
}
