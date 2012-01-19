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
import org.zanata.webtrans.shared.validation.action.VariablesValidation;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Test(groups = { "unit-tests" })
public class VariablesValidationTests
{
   private static final String MOCK_VARIABLES_VALIDATOR_NAME = "test variable validator name";
   private static final String MOCK_VARIABLES_VALIDATOR_DESCRIPTION = "test variable validator description";
   private static final String MOCK_VARIABLES_ADDED_MESSAGE = "test variables added message";
   private static final String MOCK_VARIABLES_MISSING_MESSAGE = "test variables missing message";

   private VariablesValidation variablesValidation;

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
      expect(mockMessages.variablesValidatorName()).andReturn(MOCK_VARIABLES_VALIDATOR_NAME).anyTimes();
      expect(mockMessages.variablesValidatorDescription()).andReturn(MOCK_VARIABLES_VALIDATOR_DESCRIPTION).anyTimes();
      replay(mockMessages);
   }

   @BeforeMethod
   public void init()
   {
      variablesValidation = null;

      capturedVarsAdded.reset();
      capturedVarsMissing.reset();
   }

   @Test
   public void idIsSet()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      assertThat(variablesValidation.getId(), is(MOCK_VARIABLES_VALIDATOR_NAME));
   }

   @Test
   public void descriptionIsSet()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      assertThat(variablesValidation.getDescription(), is(MOCK_VARIABLES_VALIDATOR_DESCRIPTION));
   }

   @Test
   public void noErrorForMatchingVars()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "Testing string with variable %var1 and %var2";
      String target = "%var2 and %var1 included, order not relevant";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(false));
      assertThat(variablesValidation.getError().size(), is(0));

      assertThat(capturedVarsAdded.hasCaptured(), is(false));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void missingVarInTarget()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "Testing string with variable %var1";
      String target = "Testing string with no variables";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItem("%var1"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void missingVarsThroughoutTarget()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "%var1 variables in all parts %var2 of the string %var3";
      String target = "Testing string with no variables";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), hasItems("%var1", "%var2", "%var3"));
      assertThat(capturedVarsMissing.getValue().size(), is(3));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @Test
   public void addedVarInTarget()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "Testing string with variable %var1";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItem("%var1"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void addedVarsThroughoutTarget()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "Testing string with no variables";
      String target = "%var1 variables in all parts %var2 of the string %var3";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), hasItems("%var1", "%var2", "%var3"));
      assertThat(capturedVarsAdded.getValue().size(), is(3));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @Test
   public void bothAddedAndMissingVars()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "String with %var1 and %var2 only, not 3";
      String target = "String with %var2 and %var3, not 1";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItems(MOCK_VARIABLES_ADDED_MESSAGE, MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(2));

      assertThat(capturedVarsAdded.getValue(), hasItem("%var3"));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.getValue(), hasItem("%var1"));
      assertThat(capturedVarsMissing.getValue().size(), is(1));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void substringVariablesDontMatch()
   {
      variablesValidation = new VariablesValidation(mockMessages);
      String source = "%testing";
      String target = "%test %testing";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsAdded.getValue(), allOf(hasItem("%test"), not(hasItem("%testing"))));
      assertThat(capturedVarsAdded.getValue().size(), is(1));
      assertThat(capturedVarsMissing.hasCaptured(), is(false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch()
   {
      variablesValidation = new VariablesValidation(mockMessages);

      String source = "%what %whatever";
      String target = "%whatever";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItem(MOCK_VARIABLES_MISSING_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(1));

      assertThat(capturedVarsMissing.getValue(), allOf(hasItem("%what"), not(hasItem("%whatever"))));
      assertThat(capturedVarsAdded.hasCaptured(), is(false));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void superstringVariablesDontMatch2()
   {
      variablesValidation = new VariablesValidation(mockMessages);

      String source = "%test";
      String target = "%testing";
      variablesValidation.validate(source, target);

      assertThat(variablesValidation.hasError(), is(true));
      assertThat(variablesValidation.getError(), hasItems(MOCK_VARIABLES_MISSING_MESSAGE, MOCK_VARIABLES_ADDED_MESSAGE));
      assertThat(variablesValidation.getError().size(), is(2));

      assertThat(capturedVarsMissing.getValue(), allOf(hasItem("%test"), not(hasItem("%testing"))));
      assertThat(capturedVarsAdded.getValue(), allOf(hasItem("%testing"), not(hasItem("%test"))));
   }
}


 