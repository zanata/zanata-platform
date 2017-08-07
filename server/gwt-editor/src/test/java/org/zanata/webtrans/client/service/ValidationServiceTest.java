package org.zanata.webtrans.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.zanata.webtrans.client.util.FakeValidationMessages.fakeValidationMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.client.ui.HasUpdateValidationMessage;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.validation.ValidationFactory;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ValidationServiceTest {
    public static final ValidationId VAL_KEY = ValidationId.HTML_XML;
    private ValidationService service;
    @Mock
    private EventBus eventBus;

    private ValidationMessages validationMessages;

    @Mock
    private HasUpdateValidationMessage validationMessagePanel;

    @Mock
    private UserConfigHolder configHolder;

    @Before
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        validationMessages = fakeValidationMessages();

        service =
                new ValidationService(eventBus, validationMessages,
                        configHolder);
        ValidationFactory validationFactory =
                new ValidationFactory(validationMessages);

        Collection<ValidationAction> validationList =
                validationFactory.getAllValidationActions().values();
        Map<ValidationId, State> validationStatesMap =
                new HashMap<ValidationId, State>();

        for (ValidationAction action : validationList) {
            action.getRules().setEnabled(true);
            validationStatesMap.put(action.getId(), action.getState());
        }
        service.setValidationRules(validationStatesMap);

        verify(eventBus).addHandler(RunValidationEvent.getType(), service);
    }

    @Test
    public void canUpdateValidatorStatus() {
        service.updateStatus(VAL_KEY, false, true);

        ValidationAction validationAction =
                service.getValidationMap().get(VAL_KEY);

        assertThat(validationAction.getRules().isEnabled()).isFalse();
        verify(eventBus).fireEvent(RequestValidationEvent.EVENT);
    }

    @Test
    public void canGetValidationList() {
        List<ValidationAction> validationList =
                new ArrayList<ValidationAction>(service.getValidationMap()
                        .values());

        assertThat(validationList.size()).isEqualTo(7);
    }

}
