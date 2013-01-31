package org.zanata.webtrans.client.presenter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ValidationOptionsPresenterTest
{
   private ValidationOptionsPresenter presenter;
   @Mock
   private ValidationOptionsPresenter.Display display;
   @Mock
   private EventBus eventBus;
   @Mock
   private ValidationService validationService;
   @Mock
   private ValidationMessages validationMessage;
   @Mock
   private HasValueChangeHandlers<Boolean> printfChangeHandler;
   @Mock
   private HasValueChangeHandlers<Boolean> positionalPrintfChangeHandler;
   @Captor
   private ArgumentCaptor<ValueChangeHandler<Boolean>> valueChangeHandlerCaptor;
   @Mock
   private ValueChangeEvent<Boolean> valueChangeEvent;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new ValidationOptionsPresenter(display, eventBus, validationService);
   }

   @Test
   public void onBind()
   {
      // Given:
      // List<ValidationObject> validationObjects =
      // Lists.<ValidationObject>newArrayList(new
      // PrintfVariablesValidation(validationMessage), new
      // PrintfXSIExtensionValidation(validationMessage));
      // when(validationService.getValidationList()).thenReturn(validationObjects);

      when(display.addValidationSelector("printf", "printf description", true)).thenReturn(printfChangeHandler);
      when(display.addValidationSelector("positional printf", "positional printf description", false)).thenReturn(positionalPrintfChangeHandler);

      // When:
      presenter.onBind();

      // Then:
      verify(display, times(2)).addValidationSelector(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
      verify(printfChangeHandler).addValueChangeHandler(valueChangeHandlerCaptor.capture());
      verify(positionalPrintfChangeHandler).addValueChangeHandler(valueChangeHandlerCaptor.capture());
      verifyNoMoreInteractions(display);
   }

   @Test
   public void onValidationOptionValueChanged()
   {
      // Given: validation object has mutually exclusive validation object
      // PrintfVariablesValidation printfVariablesValidation = new
      // PrintfVariablesValidation(validationMessage);
      // printfVariablesValidation.mutuallyExclusive(new
      // PrintfXSIExtensionValidation(validationMessage));
      // ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler =
      // presenter.new
      // ValidationOptionValueChangeHandler(printfVariablesValidation);

      when(valueChangeEvent.getValue()).thenReturn(true);

      // When:
      // handler.onValueChange(valueChangeEvent);

      // Then:
      verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES, true);
      verify(validationService).updateStatus(ValidationId.PRINTF_XSI_EXTENSION, false);
      verify(display).changeValidationSelectorValue("positional printf", false);
   }

   @Test
   public void onValidationOptionValueChangedWithoutMutualExclusiveValidator()
   {
      // Given: validation object has NO mutually exclusive validation object
      // PrintfVariablesValidation printfVariablesValidation = new
      // PrintfVariablesValidation(validationMessage);
      // ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler =
      // presenter.new
      // ValidationOptionValueChangeHandler(printfVariablesValidation);

      when(valueChangeEvent.getValue()).thenReturn(true);

      // When:
      // handler.onValueChange(valueChangeEvent);

      // Then:
      verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES, true);
      verifyNoMoreInteractions(validationService);
   }
}
