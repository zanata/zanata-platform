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
import java.util.List;

import org.zanata.webtrans.client.resources.ValidationMessages;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * 
 * @author David Mason, damason@redhat.com
 * 
 **/
public class JavaVariablesValidation extends ValidationAction
{
   public JavaVariablesValidation(final ValidationMessages messages)
   {
      super(messages.javaVariablesValidatorName(), messages.javaVariablesValidatorDescription(), true, messages);
   }

   //TODO improve this
   //See http://docs.oracle.com/javase/1.4.2/docs/api/java/text/MessageFormat.html#format(java.lang.String, java.lang.Object[])
   //there are some examples here for test cases
   //need to account for subformats and quotes in this.
   //recursion is possible so this may need to be done programatically (e.g. with a stack of curly braces)
   private final static String javaVarRegex = "[{][^{]*[}]";

   @Override
   public void doValidate(String source, String target)
   {
      ArrayList<String> sourceVars = findJavaVars(source);
      ArrayList<String> targetVars = findJavaVars(target);

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

   private ArrayList<String> findJavaVars(String inString)
   {
      ArrayList<String> vars = new ArrayList<String>();
      // compile each time to reset index
      RegExp javaVarRegExp = RegExp.compile(javaVarRegex, "g");
      MatchResult result = javaVarRegExp.exec(inString);
      while (result != null)
      {
         vars.add(result.getGroup(0));
         result = javaVarRegExp.exec(inString);
      }
      return vars;
   }
}
