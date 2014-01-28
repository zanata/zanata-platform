package org.zanata.events;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequiredArgsConstructor
@Getter
public class TextFlowTargetUpdateContextEvent {
    public static final String EVENT_NAME = "org.zanata.event.TextFlowTargetUpdateContextEvent";

    private final TransUnitId transUnitId;
    private final LocaleId localeId;
    private final EditorClientId editorClientId;
    private final TransUnitUpdated.UpdateType updateType;
}
