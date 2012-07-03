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

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class PrintfVariablesValidationTest
{
   private static final String MOCK_VARIABLES_VALIDATOR_NAME = "test variable validator name";
   private static final String MOCK_VARIABLES_VALIDATOR_DESCRIPTION = "test variable validator description";
   private static final String MOCK_VARIABLES_ADDED_MESSAGE = "test variables added message";
   private static final String MOCK_VARIABLES_MISSING_MESSAGE = "test variables missing message";

   private PrintfVariablesValidation printfVariablesValidation;

   private ValidationMessages mockMessages;
   private Capture<List<String>> capturedVarsAdded;
   private Capture<List<String>> capturedVarsMissing;

   @BeforeClass
   public void mockMessages()
   {
      mockMessages = createMock(ValidationMessages.class);

      capturedVarsAdded = new Capture<List<String>>();
      capturedVarsMissing = new Capture<List<String>>();

      expect(mockMessages.varsAdded(capture(capturedVarsAdded))).andReturn(MOCK_VARIABLES_ADDED_MESSAGE).anyTimes();
      expect(mockMessages.varsMissing(capture(capturedVarsMissing))).andReturn(MOCK_VARIABLES_MISSING_MESSAGE).anyTimes();
      expect(mockMessages.printfVariablesValidatorName()).andReturn(MOCK_VARIABLES_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.printfVariablesValidatorDescription()).andReturn(MOCK_VARIABLES_VALIDATOR_DESCRIPTION).anyTimes();
      replay(mockMessages);
   }

   @BeforeMethod
   public void init()
   {
      printfVariablesValidation = null;

      capturedVarsAdded.reset();
      capturedVarsMissing.reset();
   }

   @Test
   public void idIsSet()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      assertThat(printfVariablesValidation.getId(), is(MOCK_VARIABLES_VALIDATOR_NAME));
   }

   @Test
   public void descriptionIsSet()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      assertThat(printfVariablesValidation.getDescription(), is(MOCK_VARIABLES_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void noErrorForMatchingVars()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "Testing string with variable %1v and %2v";
      String target = "%2v and %1v included, order not relevant";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(false));
      assertThat(printfVariablesValidation.getError().size(), is(0));

      assertThat(capturedVarsAdded.hasCaptured(), is(false));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void missingVarInTarget()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "Testing string with variable %1v";
      String target = "Testing string with no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItem("%1v"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void missingVarsThroughoutTarget()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%a variables in all parts %b of the string %c";
      String target = "Testing string with no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItems("%a", "%b", "%c"));
      assertThat(capturedVarsMissing.getValue().size(), is(3));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void addedVarInTarget()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "Testing string with variable %2$#x";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItem("%2$#x"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void addedVarsThroughoutTarget()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "%1$-0lls variables in all parts %2$-0hs of the string %3$-0ls";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItems("%1$-0lls", "%2$-0hs", "%3$-0ls"));
      assertThat(capturedVarsAdded.getValue().size(), is(3));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void bothAddedAndMissingVars()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "String with %x and %y only, not z";
      String target = "String with %y and %z, not x";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItems(MOCK_VARIABLES_ADDED_MESSAGE, MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.getValue(), hasItem("%z"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.getValue(), hasItem("%x"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void substringVariablesDontMatch()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%ll";
      String target = "%l %ll";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), allOf(hasItem("%l"), not(hasItem("%ll"))));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);

      String source = "%l %ll";
      String target = "%ll";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), allOf(hasItem("%l"), not(hasItem("%ll"))));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch2()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);

      String source = "%z";
      String target = "%zz";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItems(MOCK_VARIABLES_MISSING_MESSAGE, MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsMissing.getValue(), allOf(hasItem("%z"), not(hasItem("%zz"))));
      assertThat(capturedVarsAdded.getValue(), allOf(hasItem("%zz"), not(hasItem("%z"))));
   }

   @Test
   public void checkWithRealWorldExamples()
   {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      // examples from strings in translate.zanata.org
      String source = "%s %d %-25s %r";
      String target = "no variables";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError(), hasItems(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(printfVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItems("%s", "%d", "%-25s", "%r"));
   }

   @Test
   public void validPositionalVariables() {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%1$s：Read error while reading %3$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(false));
      assertThat(printfVariablesValidation.getError().size(), is(0));

      assertThat(capturedVarsAdded.hasCaptured(), is(false));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void mixPositionalVariablesWithNotPositionalWillValidateAsIs() {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%1$s：Read error while reading %lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.hasCaptured(), is(true));
      assertThat(capturedVarsAdded.getValue(), contains("%1$s" , "%2$s"));
      assertThat(capturedVarsMissing.hasCaptured(), is(true));
      assertThat(capturedVarsMissing.getValue(), contains("%s", "%s"));
   }

   @Test
   public void invalidPositionalVariablesWillValidateAsIs() {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %99$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.hasCaptured(), is(true));
      assertThat(capturedVarsAdded.getValue(), contains("%3$s", "%99$lu", "%2$s"));
      assertThat(capturedVarsMissing.hasCaptured(), is(true));
      assertThat(capturedVarsMissing.getValue(), contains("%s", "%s", "%lu"));
   }

   @Test
   public void positionalVariablesHaveSamePosition() {
      printfVariablesValidation = new PrintfVariablesValidation(mockMessages);
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %3$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.hasCaptured(), is(true));
      assertThat(capturedVarsAdded.getValue(), contains("%3$s"));
      assertThat(capturedVarsMissing.hasCaptured(), is(true));
      assertThat(capturedVarsMissing.getValue(), contains("%s"));
   }
}


 