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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class NewlineLeadTrailValidationTests
{
   // TODO use TestMessages

   // mock message strings
   private static final String MOCK_NEWLINE_VALIDATOR_NAME = "test newline validator name";
   private static final String MOCK_NEWLINE_VALIDATOR_DESCRIPTION = "test xml html validator description";

   private static final String MOCK_TRAILING_NEWLINE_MISSING_MESSAGE = "mock trailing newline missing message";
   private static final String MOCK_TRAILING_NEWLINE_ADDED_MESSAGE = "mock trailing newline added message";
   private static final String MOCK_LEADING_NEWLINE_MISSING_MESSAGE = "mock leading newline missing message";
   private static final String MOCK_LEADING_NEWLINE_ADDED_MESSAGE = "mock leading newline added message";


   private NewlineLeadTrailValidation newlineLeadTrailValidation;

   @Mock
   private ValidationMessages mockMessages;

   @BeforeMethod
   public void init()
   {
      MockitoAnnotations.initMocks(this);
      newlineLeadTrailValidation = null;
      when(mockMessages.newlineValidatorName()).thenReturn(MOCK_NEWLINE_VALIDATOR_NAME);
      when(mockMessages.newlineValidatorDescription()).thenReturn(MOCK_NEWLINE_VALIDATOR_DESCRIPTION);

      when(mockMessages.leadingNewlineAdded()).thenReturn(MOCK_LEADING_NEWLINE_ADDED_MESSAGE);
      when(mockMessages.leadingNewlineMissing()).thenReturn(MOCK_LEADING_NEWLINE_MISSING_MESSAGE);
      when(mockMessages.trailingNewlineAdded()).thenReturn(MOCK_TRAILING_NEWLINE_ADDED_MESSAGE);
      when(mockMessages.trailingNewlineMissing()).thenReturn(MOCK_TRAILING_NEWLINE_MISSING_MESSAGE);
   }

   @Test
   public void idIsSet()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      assertThat(newlineLeadTrailValidation.getId(), is(MOCK_NEWLINE_VALIDATOR_NAME));
   }

   @Test
   public void descriptionIsSet()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      assertThat(newlineLeadTrailValidation.getDescription(), is(MOCK_NEWLINE_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void noNewlinesBothMatch()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "String without newlines";
      String target = "Different newline-devoid string";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(false));
      assertThat(newlineLeadTrailValidation.getError().size(), is(0));
   }

   @Test
   public void bothNewlinesBothMatch()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "\nString with both newlines\n";
      String target = "\nDifferent newline-infested string\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(false));
      assertThat(newlineLeadTrailValidation.getError().size(), is(0));
   }

   @Test
   public void internalNewlinesDontCount()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "String with an \n internal newline.";
      String target = "Different string lacking the newline";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(false));
      assertThat(newlineLeadTrailValidation.getError().size(), is(0));
   }

   @Test
   public void missingLeadingNewline()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "\nTesting string with leading new line";
      String target = "Different string with the newline removed";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(MOCK_LEADING_NEWLINE_MISSING_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void addedLeadingNewline()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "Testing string without a leading new line";
      String target = "\nDifferent string with a leading newline added";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(MOCK_LEADING_NEWLINE_ADDED_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void missingTrailingNewline()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "Testing string with trailing new line\n";
      String target = "Different string with the newline removed";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(MOCK_TRAILING_NEWLINE_MISSING_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void addedTrailingNewline()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "Testing string without a trailing new line";
      String target = "Different string with a trailing newline added\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(MOCK_TRAILING_NEWLINE_ADDED_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void addedBothNewlines()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "Testing string with no newlines";
      String target = "\nDifferent string with both added\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(MOCK_TRAILING_NEWLINE_ADDED_MESSAGE, MOCK_TRAILING_NEWLINE_ADDED_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void missingBothNewlines()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "\nString with both newlines\n";
      String target = "Other string with no newlines";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(MOCK_TRAILING_NEWLINE_MISSING_MESSAGE, MOCK_TRAILING_NEWLINE_MISSING_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedAndMissing1()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "\nString with only leading newline";
      String target = "Other string with newline trailing\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(MOCK_LEADING_NEWLINE_MISSING_MESSAGE, MOCK_TRAILING_NEWLINE_ADDED_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedAndMissing2()
   {
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(mockMessages);
      String source = "String with trailing newline\n";
      String target = "\nOther string with newline leading";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(MOCK_LEADING_NEWLINE_ADDED_MESSAGE, MOCK_TRAILING_NEWLINE_MISSING_MESSAGE));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }
}


 