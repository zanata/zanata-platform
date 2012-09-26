package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.shared.validation.ValidationObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ValidationServiceTest
{
   public static final String VAL_KEY = "a";
   private ValidationService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private TableEditorMessages messages;
   @Mock
   private HasUpdateValidationWarning validationMessagePanel;
   @Mock
   private ValidationObject validationObject;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      Map<String, ValidationObject> validationMap = ImmutableMap.<String, ValidationObject>builder().put(VAL_KEY, validationObject).build();
      service = new ValidationService(eventBus, messages, validationMap);

      when(messages.notifyValidationError()).thenReturn("validation error");

      verify(eventBus).addHandler(RunValidationEvent.getType(), service);
      verify(eventBus).addHandler(TransUnitSelectionEvent.getType(), service);
      verify(eventBus).addHandler(DocumentSelectionEvent.getType(), service);
   }

   @Test
   public void onValidate()
   {
      RunValidationEvent event = new RunValidationEvent("source", "target %s", false);
      event.addWidget(validationMessagePanel);
      when(validationObject.isEnabled()).thenReturn(true);
      ArrayList<String> errors = Lists.newArrayList("var added %s");
      when(validationObject.getError()).thenReturn(errors);

      service.onValidate(event);

      InOrder inOrder = Mockito.inOrder(validationObject);
      inOrder.verify(validationObject).clearErrorMessage();
      inOrder.verify(validationObject).validate("source", "target %s");
      verify(validationMessagePanel).updateValidationWarning(errors);
   }

   @Test
   public void onTransUnitSelectionWillClearMessages()
   {
      ValidationService validationServiceSpy = Mockito.spy(service);
      doNothing().when(validationServiceSpy).clearAllMessage();

      validationServiceSpy.onTransUnitSelected(null);

      verify(validationServiceSpy).clearAllMessage();
   }

   @Test
   public void onDocumentSelectionWillClearMessages()
   {
      ValidationService validationServiceSpy = Mockito.spy(service);
      doNothing().when(validationServiceSpy).clearAllMessage();

      validationServiceSpy.onDocumentSelected(null);

      verify(validationServiceSpy).clearAllMessage();
   }

   @Test
   public void canClearAllErrorMessages()
   {
      service.clearAllMessage();

      verify(validationObject).clearErrorMessage();
   }

   @Test
   public void canUpdateValidatorStatus()
   {
      service.updateStatus(VAL_KEY, false);

      verify(validationObject).setEnabled(false);
      verify(eventBus).fireEvent(RequestValidationEvent.EVENT);
   }

   @Test
   public void canGetValidationList()
   {
      List<ValidationObject> validationList = service.getValidationList();

      assertThat(validationList, Matchers.contains(validationObject));
   }

}
