package org.zanata.webtrans.client.presenter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ArrayList;

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
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;

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
   private HasValueChangeHandlers<Boolean> changeHandler;
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
      when(validationService.getValidationMap()).thenReturn(ValidationFactory.getAllValidationActions(validationMessage));

      when(display.addValidationSelector(ValidationId.HTML_XML.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.NEW_LINE.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.TAB.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.JAVA_VARIABLES.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.XML_ENTITY.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.PRINTF_VARIABLES.getDisplayName(), null, false)).thenReturn(changeHandler);
      when(display.addValidationSelector(ValidationId.PRINTF_XSI_EXTENSION.getDisplayName(), null, false)).thenReturn(changeHandler);


      // When:
      presenter.onBind();

      // Then:
      verify(display, times(7)).addValidationSelector(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
      verify(changeHandler, times(7)).addValueChangeHandler(valueChangeHandlerCaptor.capture());
      verifyNoMoreInteractions(display);
   }

   @Test
   public void onValidationOptionValueChanged()
   {
      // Given: validation object has mutually exclusive validation object
      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, validationMessage);
      printfVariablesValidation.mutuallyExclusive(new PrintfXSIExtensionValidation(ValidationId.PRINTF_XSI_EXTENSION, validationMessage));
      ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler = presenter.new ValidationOptionValueChangeHandler(printfVariablesValidation);

      when(valueChangeEvent.getValue()).thenReturn(true);

      // When:
      handler.onValueChange(valueChangeEvent);

      // Then:
      verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES, true);
      verify(validationService).updateStatus(ValidationId.PRINTF_XSI_EXTENSION, false);
      verify(display).changeValidationSelectorValue(ValidationId.PRINTF_XSI_EXTENSION.getDisplayName(), false);
   }

   @Test
   public void onValidationOptionValueChangedWithoutMutualExclusiveValidator()
   {
      // Given: validation object has NO mutually exclusive validation object
      PrintfVariablesValidation printfVariablesValidation = new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES, validationMessage);
      ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler = presenter.new ValidationOptionValueChangeHandler(printfVariablesValidation);

      when(valueChangeEvent.getValue()).thenReturn(true);

      // When:
      handler.onValueChange(valueChangeEvent);

      // Then:
      verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES, true);
      verifyNoMoreInteractions(validationService);
   }
}
