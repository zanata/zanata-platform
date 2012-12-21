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
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.TestMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.action.TabValidation;

@Test(groups = { "unit-tests" })
public class TabValidationTest
{
   private ValidationMessages messages;
   private TabValidation validation;
   private static final List<String> noErrors = Collections.<String>emptyList();

   @BeforeMethod
   public void init()
   {
      messages = TestMessages.getInstance(ValidationMessages.class);
      validation = new TabValidation(messages);
   }

   @Test
   public void idIsSet()
   {
      assertThat(validation.getId(), is(messages.tabValidatorName()));
   }

   @Test
   public void descriptionIsSet()
   {
      assertThat(validation.getDescription(), is(messages.tabValidatorDescription()));
   }

   @Test
   public void noTabsInEither()
   {
      String source = "Source without tab";
      String target = "Target without tab";
      validation.validate(source, target);

      assertThat(validation.getError(), is(noErrors));
      assertThat(validation.hasError(), is(false));
   }

   @Test
   public void tabsInBoth()
   {
      String source = "Source with\ttab";
      String target = "Target with\ttab";
      validation.validate(source, target);

      assertThat(validation.getError(), is(noErrors));
      assertThat(validation.hasError(), is(false));
   }

   @Test
   public void noTabsInTarget()
   {
      String source = "Source with\ttab";
      String target = "Target without tab";
      validation.validate(source, target);

      assertThat(validation.getError(), hasItem(messages.targetHasFewerTabs(1, 0)));
      assertThat(validation.getError().size(), is(1));
      assertThat(validation.hasError(), is(true));
   }

   @Test
   public void noTabsInSource()
   {
      String source = "Source without tab";
      String target = "Target with\textra tab";
      validation.validate(source, target);

      assertThat(validation.getError(), hasItem(messages.targetHasMoreTabs(0, 1)));
      assertThat(validation.getError().size(), is(1));
      assertThat(validation.hasError(), is(true));
   }

   @Test
   public void fewerTabsInTarget()
   {
      String source = "Source with two\t\t tabs";
      String target = "Target with one\ttab";
      validation.validate(source, target);

      assertThat(validation.getError(), hasItem(messages.targetHasFewerTabs(2, 1)));
      assertThat(validation.getError().size(), is(1));
      assertThat(validation.hasError(), is(true));
   }

   @Test
   public void moreTabsInTarget()
   {
      String source = "Source with one\ttab";
      String target = "Target with two\t\t tabs";
      validation.validate(source, target);

      assertThat(validation.getError(), hasItem(messages.targetHasMoreTabs(1, 2)));
      assertThat(validation.getError().size(), is(1));
      assertThat(validation.hasError(), is(true));
   }

}
