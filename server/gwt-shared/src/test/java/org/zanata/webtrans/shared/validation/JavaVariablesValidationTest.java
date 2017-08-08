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

import static org.zanata.webtrans.shared.validation.FakeValidationMessages.fakeValidationMessages;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.JavaVariablesValidation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Mason, damason@redhat.com
 **/
public class JavaVariablesValidationTest {
    private JavaVariablesValidation javaVariablesValidation;

    private ValidationMessages messages;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        messages = fakeValidationMessages();

        javaVariablesValidation =
                new JavaVariablesValidation(ValidationId.JAVA_VARIABLES,
                        messages);
        javaVariablesValidation.getRules().setEnabled(true);
    }

    @Test
    public void idIsSet() {
        assertThat(javaVariablesValidation.getId())
                .isEqualTo(ValidationId.JAVA_VARIABLES);
    }

    @Test
    public void noErrorForMatchingVars() {
        String source = "Testing string with variable {0} and {1}";
        String target = "{1} and {0} included, order not relevant";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void missingVarInTarget() {
        String source = "Testing string with variable {0}";
        String target = "Testing string with no variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList)
                .contains(messages.varsMissing(Arrays.asList("{0}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void missingVarsThroughoutTarget() {
        String source = "{0} variables in all parts {1} of the string {2}";
        String target = "Testing string with no variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList).contains(messages.varsMissing(Arrays.asList("{0}",
                "{1}", "{2}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedVarInTarget() {
        String source = "Testing string with no variables";
        String target = "Testing string with variable {0}";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList)
                .contains(messages.varsAdded(Arrays.asList("{0}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedVarsThroughoutTarget() {
        String source = "Testing string with no variables";
        String target = "{0} variables in all parts {1} of the string {2}";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList).contains(
                messages.varsAdded(Arrays.asList("{0}", "{1}", "{2}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void bothAddedAndMissingVars() {
        String source = "String with {0} and {1} only, not 2";
        String target = "String with {1} and {2}, not 0";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList).contains(messages.varsAdded(Arrays.asList("{2}")),
                messages.varsMissing(Arrays.asList("{0}")));
        assertThat(errorList.size()).isEqualTo(2);
    }

    @Test
    public void disturbanceInTheForce() {
        String source =
                "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.";
        String target =
                "At time on date, there was a disturbance in the force on planet Earth";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList).contains(messages.varsMissing(Arrays.asList("{0}",
                "{1}", "{2}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void diskContainsFiles() {
        String source = "The disk \"{1}\" contains {0} file(s).";
        String target = "The disk contains some files";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList)
                .contains(messages.varsMissing(Arrays.asList("{0}", "{1}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void doesNotDetectEscapedVariables() {
        String source = "This string does not contain \\{0\\} style variables";
        String target = "This string does not contain java style variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void doesNotDetectQuotedVariables() {
        String source = "This string does not contain '{0}' style variables";
        String target = "This string does not contain java style variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void doesNotDetectVariablesInQuotedText() {
        String source = "This 'string does not contain {0} style' variables";
        String target = "This string does not contain java style variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void ignoresEscapedQuotes() {
        String source =
                "This string does not contain \\'{0}\\' style variables";
        String target = "This string does not contain java style variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList)
                .contains(messages.varsMissing(Arrays.asList("{0}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void advancedQuoting() {
        String source = "'''{'0}'''''{0}'''";
        String target =
                "From examples on MessageFormat page, should not contain any variables";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void translatedChoicesStillMatch() {
        String source =
                "There {0,choice,0#are no things|1#is one thing|1<are many things}.";
        String target =
                "Es gibt {0,choice,0#keine Dinge|1#eine Sache|1<viele Dinge}.";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void choiceFormatAndRecursion() {
        String source =
                "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.";
        String target = "There are 0 files";
        List<String> errorList =
                javaVariablesValidation.validate(source, target);

        assertThat(errorList).contains(messages.varsMissing(Arrays.asList("{0}")));
        assertThat(errorList.size()).isEqualTo(1);
    }

    // TODO tests for format type

    // TODO test 3 or 4 levels of recursion

}
