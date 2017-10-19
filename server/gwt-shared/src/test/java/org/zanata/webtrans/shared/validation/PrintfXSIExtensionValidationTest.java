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
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PrintfXSIExtensionValidationTest {
    private PrintfXSIExtensionValidation printfXSIExtensionValidation;

    private ValidationMessages messages;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        messages = fakeValidationMessages();

        printfXSIExtensionValidation =
                new PrintfXSIExtensionValidation(
                        ValidationId.PRINTF_XSI_EXTENSION, messages);
        printfXSIExtensionValidation.getRules().setEnabled(true);
    }

    @Test
    public void idAndDescriptionAreSet() {
        assertThat(printfXSIExtensionValidation.getId())
                .isEqualTo(ValidationId.PRINTF_XSI_EXTENSION);
    }

    @Test
    public void validImplicitPositionalVariables() {
        String source = "%s: Read error at byte %s, while reading %lu byte";
        String target = "%1$s：Read error while reading %3$lu bytes，at %2$s";
        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);

        assertThat(errorList).isEmpty();
    }

    @Test
    public void validExplicitPositionalVariables() {
        String source = "%1$s: Read error at byte %2$s, while reading %3$lu byte";
        String target = "%1$s：Read error while reading %3$lu bytes，at %2$s";
        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);

        assertThat(errorList).isEmpty();
    }

    @Test
    public void mixPositionalVariablesWithNotPositional() {
        String source = "%s: Read error at byte %s, while reading %lu byte";
        String target = "%1$s：Read error while reading %lu bytes，at %2$s";
        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(3);

        assertThat(errorList).contains(
                messages.varsMissing(Arrays.asList("%3$lu")),
                messages.varsAdded(Arrays.asList("%lu")),
                messages.mixVarFormats());
    }

    @Test
    public void positionalVariableOutOfRange() {
        String source = "%s: Read error at byte %s, while reading %lu byte";
        String target = "%3$s：Read error while reading %99$lu bytes，at %2$s";
        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(3);

        assertThat(errorList).contains(
                messages.varPositionOutOfRange("%99$lu"),
                messages.varsMissing(Arrays.asList("%1$s", "%3$lu")),
                messages.varsAdded(Arrays.asList("%3$s", "%99$lu")));
    }

    @Test
    public void positionalVariablesHaveSamePosition() {
        String source = "%s: Read error at byte %s, while reading %lu byte";
        String target = "%3$s：Read error while reading %3$lu bytes, at %2$s";

        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);

        assertThat(errorList.size()).isEqualTo(3);
        assertThat(errorList).contains(
                messages.varsMissing(Arrays.asList("%1$s")),
                messages.varsAdded(Arrays.asList("%3$s")), messages
                        .varPositionDuplicated(Arrays.asList("%3$s",
                                "%3$lu")));
    }

    @Test
    public void invalidPositionalVariablesBringItAll() {
        String source = "%s of %d and %lu";
        String target = "%2$d %2$s %9$lu %z";
        List<String> errorList =
                printfXSIExtensionValidation.validate(source, target);
        assertThat(errorList).contains(
                messages.varPositionOutOfRange("%9$lu"),
                messages.mixVarFormats(),
                messages.varPositionDuplicated(Arrays.asList("%2$d", "%2$s")),
                messages.varsMissing(Arrays.asList("%1$s", "%3$lu")),
                messages.varsAdded(Arrays.asList("%2$s", "%9$lu", "%z")));
    }
}
