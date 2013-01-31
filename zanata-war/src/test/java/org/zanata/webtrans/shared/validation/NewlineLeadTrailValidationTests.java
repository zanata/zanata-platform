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

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.TestMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class NewlineLeadTrailValidationTests
{
   // TODO use TestMessages

   private NewlineLeadTrailValidation newlineLeadTrailValidation;

   @Mock
   private ValidationMessages messages;

   private static final String MOCK_NEWLINE_VALIDATOR_DESCRIPTION = "NewlineLeadTrailValidation description";

   @BeforeMethod
   public void init()
   {
      messages = TestMessages.getInstance(ValidationMessages.class);
      newlineLeadTrailValidation = new NewlineLeadTrailValidation(ValidationId.NEW_LINE, MOCK_NEWLINE_VALIDATOR_DESCRIPTION, true, messages);
   }

   @Test
   public void idIsSet()
   {
      assertThat(newlineLeadTrailValidation.getId(), is(ValidationId.NEW_LINE));
   }

   @Test
   public void descriptionIsSet()
   {
      assertThat(newlineLeadTrailValidation.getDescription(), is(MOCK_NEWLINE_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void noNewlinesBothMatch()
   {
      String source = "String without newlines";
      String target = "Different newline-devoid string";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(false));
      assertThat(newlineLeadTrailValidation.getError().size(), is(0));
   }

   @Test
   public void bothNewlinesBothMatch()
   {
      String source = "\nString with both newlines\n";
      String target = "\nDifferent newline-infested string\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(false));
      assertThat(newlineLeadTrailValidation.getError().size(), is(0));
   }

   @Test
   public void internalNewlineAdded()
   {
      String source = "String without an internal newline.";
      String target = "Different string \n containing a newline";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(messages.linesAdded(1, 2)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void internalNewlineRemoved()
   {
      String source = "String with an \n internal newline.";
      String target = "Different string lacking the newline";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItem(messages.linesRemoved(2, 1)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(1));
   }

   @Test
   public void missingLeadingNewline()
   {
      String source = "\nTesting string with leading new line";
      String target = "Different string with the newline removed";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineMissing(), messages.linesRemoved(2, 1)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedLeadingNewline()
   {
      String source = "Testing string without a leading new line";
      String target = "\nDifferent string with a leading newline added";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineAdded(), messages.linesAdded(1, 2)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void missingTrailingNewline()
   {
      String source = "Testing string with trailing new line\n";
      String target = "Different string with the newline removed";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.trailingNewlineMissing(), messages.linesRemoved(2, 1)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedTrailingNewline()
   {
      String source = "Testing string without a trailing new line";
      String target = "Different string with a trailing newline added\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.trailingNewlineAdded(), messages.linesAdded(1, 2)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedBothNewlines()
   {
      String source = "Testing string with no newlines";
      String target = "\nDifferent string with both added\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineAdded(), messages.trailingNewlineAdded(), messages.linesAdded(1, 3)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(3));
   }

   @Test
   public void missingBothNewlines()
   {
      String source = "\nString with both newlines\n";
      String target = "Other string with no newlines";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineMissing(), messages.trailingNewlineMissing(), messages.linesRemoved(3, 1)));
      assertThat(newlineLeadTrailValidation.getError().size(), is(3));
   }

   @Test
   public void addedAndMissing1()
   {
      String source = "\nString with only leading newline";
      String target = "Other string with newline trailing\n";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineMissing(), messages.trailingNewlineAdded()));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }

   @Test
   public void addedAndMissing2()
   {
      String source = "String with trailing newline\n";
      String target = "\nOther string with newline leading";
      newlineLeadTrailValidation.validate(source, target);

      assertThat(newlineLeadTrailValidation.hasError(), is(true));
      assertThat(newlineLeadTrailValidation.getError(), hasItems(messages.leadingNewlineAdded(), messages.trailingNewlineMissing()));
      assertThat(newlineLeadTrailValidation.getError().size(), is(2));
   }
}


 