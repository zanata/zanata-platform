package org.zanata.webtrans.shared.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.TestMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class PrintfXSIExtensionValidationTest
{
   private PrintfXSIExtensionValidation printfVariablesValidation;

   private ValidationMessages messages;

   @BeforeMethod
   public void init()
   {
      MockitoAnnotations.initMocks(this);

      messages = TestMessages.getInstance(ValidationMessages.class);

      printfVariablesValidation = new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, messages);
      printfVariablesValidation.getValidationInfo().setEnabled(true);
   }

   @Test
   public void idAndDescriptionAreSet()
   {
      assertThat(printfVariablesValidation.getValidationInfo().getId(), is(ValidationId.PRINTF_XSI_EXTENSION));
   }

   @Test
   public void validPositionalVariables() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%1$s：Read error while reading %3$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(false));
      assertThat(printfVariablesValidation.getError().size(), is(0));
   }

   @Test
   public void mixPositionalVariablesWithNotPositional() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%1$s：Read error while reading %lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(3));

      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varsMissing(null), messages.varsAdded(null), messages.mixVarFormats()));
   }

   @Test
   public void positionalVariableOutOfRange() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %99$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(3));

      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varPositionOutOfRange(""), messages.varsMissing(null), messages.varsAdded(null)));
   }

   @Test
   public void positionalVariablesHaveSamePosition() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %3$lu bytes, at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(3));

      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varsMissing(null), messages.varsAdded(null), messages.varPositionDuplicated(null)));
   }

   @Test
   public void invalidPositionalVariablesBringItAll() {
      String source = "%s of %d and %lu";
      String target = "%2$d %2$s %9$lu %z";
      printfVariablesValidation.validate(source, target);
      
      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varPositionOutOfRange(""), messages.mixVarFormats(), messages.varPositionDuplicated(null), messages.varsMissing(null), messages.varsAdded(null)));
   }
}
