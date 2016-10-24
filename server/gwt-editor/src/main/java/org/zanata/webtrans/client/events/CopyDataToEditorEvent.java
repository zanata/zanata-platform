package org.zanata.webtrans.client.events;

import java.util.List;

import com.google.gwt.event.shared.GwtEvent;

public class CopyDataToEditorEvent extends GwtEvent<CopyDataToEditorHandler> {

    /**
     * Handler type.
     */
    private static Type<CopyDataToEditorHandler> TYPE;

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<CopyDataToEditorHandler> getType() {
        return TYPE != null ? TYPE : (TYPE =
                new Type<CopyDataToEditorHandler>());
    }

    private List<String> targetResult;

    /**
     * @param targetResult
     */
    public CopyDataToEditorEvent(List<String> targetResult) {
        this.targetResult = targetResult;
    }

    @Override
    protected void dispatch(CopyDataToEditorHandler handler) {
        handler.onDataCopy(this);
    }

    @Override
    public GwtEvent.Type<CopyDataToEditorHandler> getAssociatedType() {
        return getType();
    }

    public List<String> getTargetResult() {
        return targetResult;
    }
}
