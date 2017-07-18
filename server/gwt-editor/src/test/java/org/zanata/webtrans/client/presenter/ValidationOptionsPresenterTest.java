package org.zanata.webtrans.client.presenter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.client.util.FakeValidationMessages.fakeValidationMessages;

import java.io.IOException;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.DocValidationResultEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.client.view.ValidationOptionsDisplay;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;
import org.zanata.webtrans.shared.validation.action.PrintfVariablesValidation;
import org.zanata.webtrans.shared.validation.action.PrintfXSIExtensionValidation;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ValidationOptionsPresenterTest {
    private ValidationOptionsPresenter presenter;
    @Mock
    private ValidationOptionsDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private ValidationService validationService;

    private ValidationMessages validationMessage;
    @Mock
    private HasValueChangeHandlers<Boolean> changeHandler;
    @Captor
    private ArgumentCaptor<ValueChangeHandler<Boolean>> valueChangeHandlerCaptor;
    @Mock
    private ValueChangeEvent<Boolean> valueChangeEvent;
    @Mock
    private WebTransMessages messages;

    private ValidationFactory validationFactory;

    @Before
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        validationMessage = fakeValidationMessages();

        validationFactory = new ValidationFactory(validationMessage);

        presenter =
                new ValidationOptionsPresenter(display, eventBus,
                        validationService, messages);
    }

    @Test
    public void onBind() {
        // Given:
        when(validationService.getValidationMap()).thenReturn(
                validationFactory.getAllValidationActions());

        when(
                display.addValidationSelector(
                        ValidationId.HTML_XML.getDisplayName(),
                        validationMessage.xmlHtmlValidatorDesc(), true, false))
                .thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.NEW_LINE.getDisplayName(),
                        validationMessage.newLineValidatorDesc(), true, false))
                .thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.TAB.getDisplayName(),
                        validationMessage.tabValidatorDesc(), true, false))
                .thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.JAVA_VARIABLES.getDisplayName(),
                        validationMessage.javaVariablesValidatorDesc(), true,
                        false)).thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.XML_ENTITY.getDisplayName(),
                        validationMessage.xmlEntityValidatorDesc(), true, false))
                .thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.PRINTF_VARIABLES.getDisplayName(),
                        validationMessage.printfVariablesValidatorDesc(), true,
                        false)).thenReturn(changeHandler);
        when(
                display.addValidationSelector(
                        ValidationId.PRINTF_XSI_EXTENSION.getDisplayName(),
                        validationMessage.printfXSIExtensionValidationDesc(),
                        false, false)).thenReturn(changeHandler);

        // When:
        presenter.onBind();

        // Then:
        verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                presenter);
        verify(eventBus).addHandler(DocValidationResultEvent.getType(),
                presenter);
        verify(display, times(7))
                .addValidationSelector(Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyBoolean(),
                        Mockito.anyBoolean());
        verify(changeHandler, times(7)).addValueChangeHandler(
                valueChangeHandlerCaptor.capture());
    }

    @Test
    public void onValidationOptionValueChanged() {
        // Given: validation object has mutually exclusive validation object
        PrintfVariablesValidation printfVariablesValidation =
                new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES,
                        validationMessage);
        printfVariablesValidation
                .mutuallyExclusive(new PrintfXSIExtensionValidation(
                        ValidationId.PRINTF_XSI_EXTENSION, validationMessage));
        ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler =
                presenter.new ValidationOptionValueChangeHandler(
                        printfVariablesValidation);

        when(valueChangeEvent.getValue()).thenReturn(true);

        // When:
        handler.onValueChange(valueChangeEvent);

        // Then:
        verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES,
                true, false);
        verify(validationService).updateStatus(
                ValidationId.PRINTF_XSI_EXTENSION, false, false);
        verify(display).changeValidationSelectorValue(
                ValidationId.PRINTF_XSI_EXTENSION.getDisplayName(), false);
    }

    @Test
    public void onValidationOptionValueChangedWithoutMutualExclusiveValidator() {
        // Given: validation object has NO mutually exclusive validation object
        PrintfVariablesValidation printfVariablesValidation =
                new PrintfVariablesValidation(ValidationId.PRINTF_VARIABLES,
                        validationMessage);
        ValidationOptionsPresenter.ValidationOptionValueChangeHandler handler =
                presenter.new ValidationOptionValueChangeHandler(
                        printfVariablesValidation);

        when(valueChangeEvent.getValue()).thenReturn(true);

        // When:
        handler.onValueChange(valueChangeEvent);

        // Then:
        verify(validationService).updateStatus(ValidationId.PRINTF_VARIABLES,
                true, false);
        verifyNoMoreInteractions(validationService);
    }
}
