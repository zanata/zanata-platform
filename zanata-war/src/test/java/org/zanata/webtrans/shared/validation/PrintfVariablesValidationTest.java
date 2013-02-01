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

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class PrintfVariablesValidationTest
{
   // TODO use TestMessages

   private static final String MOCK_VARIABLES_ADDED_MESSAGE = "test variables added message";
   private static final String MOCK_VARIABLES_MISSING_MESSAGE = "test variables missing message";

   private PrintfVariablesValidation printfVariablesValidation;

   @Mock
   private ValidationMessages mockMessages;
   @Captor
   private ArgumentCaptor<List<String>> capturedVarsAdded;
   @Captor
   private ArgumentCaptor<List<String>> capturedVarsMissing;

   @BeforeMethod
   public void init()
   {
      MockitoAnnotations.initMocks(this);
      
      when(mockMessages.varsAdded(capturedVarsAdded.capture())).thenReturn(MOCK_VARIABLES_ADDED_MESSAGE);
      when(mockMessages.varsMissing(capturedVarsMissing.capture())).thenReturn(MOCK_VARIABLES_MISSING_MESSAGE);

      printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES,mockMessages);
      printfVariablesValidation.setEnabled(true);
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
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(false));
      assertThat(printfVariablesValidation.getError().size(), is(0));
   }

   @Test
   public void missingVarInTarget()
   {
      String source = "Testing string with variable %1v";
      String target = "Testing string with no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), contains("%1v"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
   }

   @Test
   public void missingVarsThroughoutTarget()
   {
      String source = "%a variables in all parts %b of the string %c";
      String target = "Testing string with no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), contains("%a", "%b", "%c"));
      assertThat(capturedVarsMissing.getValue().size(), is(3));
   }

   @Test
   public void addedVarInTarget()
   {
      String source = "Testing string with no variables";
      String target = "Testing string with variable %2$#x";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), contains("%2$#x"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
   }

   @Test
   public void addedVarsThroughoutTarget()
   {
      String source = "Testing string with no variables";
      String target = "%1$-0lls variables in all parts %2$-0hs of the string %3$-0ls";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), contains("%1$-0lls", "%2$-0hs", "%3$-0ls"));
      assertThat(capturedVarsAdded.getValue().size(), is(3));
   }

   @Test
   public void bothAddedAndMissingVars()
   {
      String source = "String with %x and %y only, not z";
      String target = "String with %y and %z, not x";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(MOCK_VARIABLES_ADDED_MESSAGE, MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.getValue(), contains("%z"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.getValue(), contains("%x"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void substringVariablesDontMatch()
   {
      String source = "%ll";
      String target = "%l %ll";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), allOf(contains("%l"), not(contains("%ll"))));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch()
   {
      String source = "%l %ll";
      String target = "%ll";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), allOf(contains("%l"), not(contains("%ll"))));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch2()
   {
      String source = "%z";
      String target = "%zz";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_MISSING_MESSAGE, MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsMissing.getValue(), allOf(contains("%z"), not(contains("%zz"))));
      assertThat(capturedVarsAdded.getValue(), allOf(contains("%zz"), not(contains("%z"))));
   }

   @Test
   public void checkWithRealWorldExamples()
   {
      // examples from strings in translate.zanata.org
      String source = "%s %d %-25s %r";
      String target = "no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), contains(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), contains("%s", "%d", "%-25s", "%r"));
   }
}


 