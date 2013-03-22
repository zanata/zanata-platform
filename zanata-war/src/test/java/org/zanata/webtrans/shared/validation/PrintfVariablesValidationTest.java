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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
@Test(groups = { "unit-tests" })
public class PrintfVariablesValidationTest
{
   private PrintfVariablesValidation printfVariablesValidation;

   private ValidationMessages messages;

   @BeforeMethod
   public void init() throws IOException
   {
      MockitoAnnotations.initMocks(this);

      messages = Gwti18nReader.create(ValidationMessages.class);

      printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, messages);
      printfVariablesValidation.getValidationInfo().setEnabled(true);
   }

   @Test
   public void idIsSet()
   {
      assertThat(printfVariablesValidation.getId(), is(ValidationId.PRINTF_VARIABLES));
   }

   @Test
   public void noErrorForMatchingVars()
   {
      String source = "Testing string with variable %1v and %2v";
      String target = "%2v and %1v included, order not relevant";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      assertThat(errorList.size(), is(0));
   }

   @Test
   public void missingVarInTarget()
   {
      String source = "Testing string with variable %1v";
      String target = "Testing string with no variables";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsMissing(Arrays.asList("%1v"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void missingVarsThroughoutTarget()
   {
      String source = "%a variables in all parts %b of the string %c";
      String target = "Testing string with no variables";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsMissing(Arrays.asList("%a", "%b", "%c"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void addedVarInTarget()
   {
      String source = "Testing string with no variables";
      String target = "Testing string with variable %2$#x";

      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsAdded(Arrays.asList("%2$#x"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void addedVarsThroughoutTarget()
   {
      String source = "Testing string with no variables";
      String target = "%1$-0lls variables in all parts %2$-0hs of the string %3$-0ls";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsAdded(Arrays.asList("%1$-0lls", "%2$-0hs", "%3$-0ls"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void bothAddedAndMissingVars()
   {
      String source = "String with %x and %y only, not z";
      String target = "String with %y and %z, not x";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, containsInAnyOrder(messages.varsAdded(Arrays.asList("%z")), messages.varsMissing(Arrays.asList("%x"))));
      assertThat(errorList.size(), is(2));

   }

   @Test
   public void substringVariablesDontMatch()
   {
      String source = "%ll";
      String target = "%l %ll";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsAdded(Arrays.asList("%l"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void superstringVariablesDontMatch()
   {
      String source = "%l %ll";
      String target = "%ll";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsMissing(Arrays.asList("%l"))));
      assertThat(errorList.size(), is(1));
   }

   @Test
   public void superstringVariablesDontMatch2()
   {
      String source = "%z";
      String target = "%zz";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsMissing(Arrays.asList("%z")), messages.varsAdded(Arrays.asList("%zz"))));
      assertThat(errorList.size(), is(2));
   }

   @Test
   public void checkWithRealWorldExamples()
   {
      // examples from strings in translate.zanata.org
      String source = "%s %d %-25s %r";
      String target = "no variables";
      List<String> errorList = printfVariablesValidation.validate(source, target);

      
      assertThat(errorList, contains(messages.varsMissing(Arrays.asList("%s", "%d", "%-25s", "%r"))));
      assertThat(errorList.size(), is(1));
   }
}
