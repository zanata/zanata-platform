package org.zanata.webtrans.client.presenter;

import java.util.Map;

import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class EditorTranslators {
    private final UserSessionService sessionService;
    private final Identity identity;

    @Inject
    public EditorTranslators(UserSessionService sessionService,
            Identity identity) {
        this.sessionService = sessionService;
        this.identity = identity;
    }

    public void updateTranslator(TargetContentsDisplay display,
            TransUnitId currentTransUnitId) {
        for (Map.Entry<EditorClientId, UserPanelSessionItem> entry : sessionService
                .getUserSessionMap().entrySet()) {
            EditorClientId editorClientId = entry.getKey();
            UserPanelSessionItem panelSessionItem = entry.getValue();
            if (panelSessionItem.getSelectedId() != null) {
                updateEditorTranslatorList(display,
                        panelSessionItem.getSelectedId(),
                        panelSessionItem.getPerson(), editorClientId,
                        currentTransUnitId);
            }
        }
    }

    private void updateEditorTranslatorList(TargetContentsDisplay display,
            TransUnitId selectedTransUnitId,
            Person person, EditorClientId editorClientId,
            TransUnitId currentTransUnitId) {
        if (!editorClientId.equals(identity.getEditorClientId())
                && Objects.equal(currentTransUnitId, selectedTransUnitId)) {
            display.addTranslator(person.getName(),
                    sessionService.getColor(editorClientId));
        } else {
            display.removeTranslator(person.getName(),
                    sessionService.getColor(editorClientId));
        }
    }
}
