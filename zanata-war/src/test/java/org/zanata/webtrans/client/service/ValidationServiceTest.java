package org.zanata.webtrans.client.service;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;

import com.google.common.collect.Lists;

import net.customware.gwt.presenter.client.EventBus;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ValidationServiceTest
{
   private ValidationService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private TableEditorMessages messages;
   @Mock
   private ValidationMessages valMessages;
   @Mock
   private HasUpdateValidationWarning validationMessagePanel;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      service = new ValidationService(eventBus, messages, valMessages);

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
      service.onValidate(event);

      verify(validationMessagePanel).updateValidationWarning(Lists.<String>newArrayList());
   }

}
