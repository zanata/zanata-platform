package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.client.ui.HasUpdateValidationMessage;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.ValidationDisplayRules;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
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

    @BeforeMethod
    public void beforeMethod() throws IOException {
        MockitoAnnotations.initMocks(this);

        validationMessages = Gwti18nReader.create(ValidationMessages.class);

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

        assertThat(validationAction.getRules().isEnabled(),
                Matchers.equalTo(false));
        verify(eventBus).fireEvent(RequestValidationEvent.EVENT);
    }

    @Test
    public void canGetValidationList() {
        List<ValidationAction> validationList =
                new ArrayList<ValidationAction>(service.getValidationMap()
                        .values());

        assertThat(validationList.size(), Matchers.equalTo(7));
    }

}
