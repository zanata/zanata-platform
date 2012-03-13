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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;

/**
 *
 * @author David Mason, damason@redhat.com
 *
 **/
@Test(groups = { "unit-tests" })
public class JavaVariablesValidationTest
{
   private static final String MOCK_VARIABLES_VALIDATOR_NAME = "test variable validator name";
   private static final String MOCK_VARIABLES_VALIDATOR_DESCRIPTION = "test variable validator description";
   private static final String MOCK_VARIABLES_ADDED_MESSAGE = "test variables added message";
   private static final String MOCK_VARIABLES_MISSING_MESSAGE = "test variables missing message";

   private JavaVariablesValidation javaVariablesValidation;

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
      expect(mockMessages.javaVariablesValidatorName()).andReturn(MOCK_VARIABLES_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.javaVariablesValidatorDescription()).andReturn(MOCK_VARIABLES_VALIDATOR_DESCRIPTION).anyTimes();
      replay(mockMessages);
   }

   @BeforeMethod
   public void init()
   {
      javaVariablesValidation = null;

      capturedVarsAdded.reset();
      capturedVarsMissing.reset();
   }

   @Test
   public void idIsSet()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      assertThat(javaVariablesValidation.getId(), is(MOCK_VARIABLES_VALIDATOR_NAME));
   }

   @Test
   public void descriptionIsSet()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      assertThat(javaVariablesValidation.getDescription(), is(MOCK_VARIABLES_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void noErrorForMatchingVars()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "Testing string with variable {0} and {1}";
      String target = "{1} and {0} included, order not relevant";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(false));
      assertThat(javaVariablesValidation.getError().size(), is(0));

      assertThat(capturedVarsAdded.hasCaptured(), is(false));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void missingVarInTarget()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "Testing string with variable {0}";
      String target = "Testing string with no variables";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(true));
      assertThat(javaVariablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(javaVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItem("{0}"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void missingVarsThroughoutTarget()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "{0} variables in all parts {1} of the string {2}";
      String target = "Testing string with no variables";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(true));
      assertThat(javaVariablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(javaVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItems("{0}", "{1}", "{2}"));
      assertThat(capturedVarsMissing.getValue().size(), is(3));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void addedVarInTarget()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "Testing string with variable {0}";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(true));
      assertThat(javaVariablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(javaVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItem("{0}"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void addedVarsThroughoutTarget()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "{0} variables in all parts {1} of the string {2}";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(true));
      assertThat(javaVariablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(javaVariablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItems("{0}", "{1}", "{2}"));
      assertThat(capturedVarsAdded.getValue().size(), is(3));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void bothAddedAndMissingVars()
   {
      javaVariablesValidation = new JavaVariablesValidation(mockMessages);
      String source = "String with {0} and {1} only, not 2";
      String target = "String with {1} and {2}, not 0";
      javaVariablesValidation.validate(source, target);

      assertThat(javaVariablesValidation.hasError(), is(true));
      assertThat(javaVariablesValidation.getError(), hasItems(MOCK_VARIABLES_ADDED_MESSAGE, MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(javaVariablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.getValue(), hasItem("{2}"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.getValue(), hasItem("{0}"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
   }

}


 