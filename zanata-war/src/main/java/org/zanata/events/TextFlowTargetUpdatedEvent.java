package org.zanata.events;

import lombok.Value;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

/**
 * This event is raised in Hibernate entity listener after change has been made.
 *
 * We then gather all relevant information required for TransUnitUpdated object
 * creation.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see org.zanata.webtrans.shared.rpc.TransUnitUpdated
 * @see org.zanata.webtrans.server.rpc.TransUnitUpdateHelper
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Value
public class TextFlowTargetUpdatedEvent {
    public static final String EVENT_NAME =
            "org.zanata.events.TextFlowTargetUpdatedEvent";

    private final TranslationWorkspace workspace;
    private final Long textFlowTargetId;
    private final TransUnitUpdated transUnitUpdated;
}
