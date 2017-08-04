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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.NewlineLeadTrailValidation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class NewlineLeadTrailValidationTest {
    private NewlineLeadTrailValidation newlineLeadTrailValidation;

    private ValidationMessages messages;

    @Before
    public void init() throws IOException {
        messages = fakeValidationMessages();

        newlineLeadTrailValidation =
                new NewlineLeadTrailValidation(ValidationId.NEW_LINE, messages);
        newlineLeadTrailValidation.getRules().setEnabled(true);
    }

    @Test
    public void idIsSet() {
        assertThat(newlineLeadTrailValidation.getId()).isEqualTo(ValidationId.NEW_LINE);
    }

    @Test
    public void noNewlinesBothMatch() {
        String source = "String without newlines";
        String target = "Different newline-devoid string";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void bothNewlinesBothMatch() {
        String source = "\nString with both newlines\n";
        String target = "\nDifferent newline-infested string\n";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(0);
    }

    @Test
    public void missingLeadingNewline() {
        String source = "\nTesting string with leading new line";
        String target = "Different string with the newline removed";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineMissing());
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedLeadingNewline() {
        String source = "Testing string without a leading new line";
        String target = "\nDifferent string with a leading newline added";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineAdded());
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void missingTrailingNewline() {
        String source = "Testing string with trailing new line\n";
        String target = "Different string with the newline removed";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.trailingNewlineMissing());
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedTrailingNewline() {
        String source = "Testing string without a trailing new line";
        String target = "Different string with a trailing newline added\n";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.trailingNewlineAdded());
        assertThat(errorList.size()).isEqualTo(1);
    }

    @Test
    public void addedBothNewlines() {
        String source = "Testing string with no newlines";
        String target = "\nDifferent string with both added\n";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineAdded(),
                messages.trailingNewlineAdded());
        assertThat(errorList.size()).isEqualTo(2);
    }

    @Test
    public void missingBothNewlines() {
        String source = "\nString with both newlines\n";
        String target = "Other string with no newlines";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineMissing(),
                messages.trailingNewlineMissing());
        assertThat(errorList.size()).isEqualTo(2);
    }

    @Test
    public void addedAndMissing1() {
        String source = "\nString with only leading newline";
        String target = "Other string with newline trailing\n";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineMissing(),
                messages.trailingNewlineAdded());
        assertThat(errorList.size()).isEqualTo(2);
    }

    @Test
    public void addedAndMissing2() {
        String source = "String with trailing newline\n";
        String target = "\nOther string with newline leading";
        List<String> errorList =
                newlineLeadTrailValidation.validate(source, target);

        assertThat(errorList).contains(messages.leadingNewlineAdded(),
                messages.trailingNewlineMissing());
        assertThat(errorList.size()).isEqualTo(2);
    }
}
