package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;

public class GetValidationRulesResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private Map<ValidationId, State> validationRules;

    @SuppressWarnings("unused")
    private GetValidationRulesResult() {
    }

    public GetValidationRulesResult(Map<ValidationId, State> validations) {
        this.validationRules = validations;
    }

    public Map<ValidationId, State> getValidationRules() {
        return validationRules;
    }
}
