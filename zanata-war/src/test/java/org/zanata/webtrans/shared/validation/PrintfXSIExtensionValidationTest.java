package org.zanata.webtrans.shared.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.locale.Gwti18nReader;
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
   public void init() throws IOException
   {
      MockitoAnnotations.initMocks(this);

      messages = Gwti18nReader.create(ValidationMessages.class);

      printfVariablesValidation = new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, messages);
      printfVariablesValidation.getValidationInfo().setEnabled(true);
   }

   @Test
   public void idAndDescriptionAreSet()
   {
      assertThat(printfVariablesValidation.getId(), is(ValidationId.PRINTF_XSI_EXTENSION));
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

      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varsMissing(Arrays.asList("%3$lu")), messages.varsAdded(Arrays.asList("%lu")), messages.mixVarFormats()));
   }

   @Test
   public void positionalVariableOutOfRange() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %99$lu bytes，at %2$s";
      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(3));

      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varPositionOutOfRange("%99$lu"), messages.varsMissing(Arrays.asList("%1$s", "%3$lu")), messages.varsAdded(Arrays.asList("%3$s", "%99$lu"))));
   }

   @Test
   public void positionalVariablesHaveSamePosition() {
      String source = "%s: Read error at byte %s, while reading %lu byte";
      String target = "%3$s：Read error while reading %3$lu bytes, at %2$s";

      printfVariablesValidation.validate(source, target);

      assertThat(printfVariablesValidation.hasError(), is(true));
      assertThat(printfVariablesValidation.getError().size(), is(3));
      assertThat(printfVariablesValidation.getError(), containsInAnyOrder(messages.varsMissing(Arrays.asList("%1$s")), messages.varsAdded(Arrays.asList("%3$s")), messages.varPositionDuplicated(Arrays.asList("%3$s", "%3$lu"))));
   }

   @Test
   public void invalidPositionalVariablesBringItAll() {
      String source = "%s of %d and %lu";
      String target = "%2$d %2$s %9$lu %z";
      printfVariablesValidation.validate(source, target);
      
      assertThat(printfVariablesValidation.getError(), hasItem(messages.varPositionOutOfRange("%9$lu")));
      assertThat(printfVariablesValidation.getError(), hasItem(messages.mixVarFormats()));
      assertThat(printfVariablesValidation.getError(), hasItem(messages.varPositionDuplicated(Arrays.asList("%2$d", "%2$s"))));
      assertThat(printfVariablesValidation.getError(), hasItem(messages.varsMissing(Arrays.asList("%1$s", "%3$lu"))));
      assertThat(printfVariablesValidation.getError(), hasItem(messages.varsAdded(Arrays.asList("%2$s", "%9$lu", "%z"))));
   }
}
