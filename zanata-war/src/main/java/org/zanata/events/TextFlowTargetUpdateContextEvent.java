package org.zanata.events;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import lombok.Value;

/**
 * This event is raised by text flow target update initiator before making a
 * change. Hibernate entity listener will be triggered after the change is made
 * and at that point this context information is retrieved from cache.
 *
 * @see org.zanata.webtrans.server.TranslationUpdateListener
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Value
public class TextFlowTargetUpdateContextEvent {
    public static final String EVENT_NAME =
            "org.zanata.events.TextFlowTargetUpdateContextEvent";

    private final TransUnitId transUnitId;
    private final LocaleId localeId;
    private final EditorClientId editorClientId;
    private final TransUnitUpdated.UpdateType updateType;
}
