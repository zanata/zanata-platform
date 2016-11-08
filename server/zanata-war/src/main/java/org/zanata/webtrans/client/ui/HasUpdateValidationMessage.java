package org.zanata.webtrans.client.ui;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.shared.model.ValidationAction;

public interface HasUpdateValidationMessage {
    void updateValidationMessages(Map<ValidationAction, List<String>> messages);
}
