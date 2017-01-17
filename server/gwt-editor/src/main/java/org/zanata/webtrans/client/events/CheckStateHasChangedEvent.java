package org.zanata.webtrans.client.events;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.common.util.ContentStateUtil;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class CheckStateHasChangedEvent extends
        GwtEvent<CheckStateHasChangedHandler> {
    /**
     * Handler type.
     */
    public static final Type<CheckStateHasChangedHandler> TYPE =
            new Type<>();

    private final TransUnitId transUnitId;
    private final List<String> targets;
    private final ContentState status;
    private ContentState adjustedState;

    public CheckStateHasChangedEvent(TransUnitId transUnitId,
            List<String> targets, ContentState status) {
        this.transUnitId = transUnitId;
        this.targets = targets;
        this.status = status;
        adjustedState = adjustState(targets, status);
    }

    @Override
    protected void dispatch(CheckStateHasChangedHandler handler) {
        handler.onCheckStateHasChanged(this);
    }

    @Override
    public GwtEvent.Type<CheckStateHasChangedHandler> getAssociatedType() {
        return TYPE;
    }

    public TransUnitId getTransUnitId() {
        return transUnitId;
    }

    public List<String> getTargets() {
        return targets;
    }

    public ContentState getStatus() {
        return status;
    }

    public ContentState getAdjustedState() {
        return adjustedState;
    }

    /**
     *
     *
     * @param newContents
     *            new target contents
     * @param requestedState
     *            requested state by user
     * @see org.zanata.service.impl.TranslationServiceImpl#adjustContentsAndState
     */
    public static ContentState adjustState(List<String> newContents,
            ContentState requestedState) {
        if (newContents == null) {
            return ContentState.New;
        }
        return ContentStateUtil.determineState(requestedState, newContents);
    }
}
