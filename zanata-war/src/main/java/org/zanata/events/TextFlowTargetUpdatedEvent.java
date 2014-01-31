package org.zanata.events;

import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequiredArgsConstructor
@Getter
public class TextFlowTargetUpdatedEvent {
    public static final String EVENT_NAME = "org.zanata.events.TextFlowTargetUpdatedEvent";

    private final TranslationWorkspace workspace;
    private final Long textFlowTargetId;
    private final TransUnitUpdated transUnitUpdated;
}
