package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasEnterWorkspaceData;

import com.google.gwt.event.shared.GwtEvent;

public class EnterWorkspaceEvent extends GwtEvent<EnterWorkspaceEventHandler>
        implements HasEnterWorkspaceData {

    private final Person person;
    private final EditorClientId editorClientId;

    /**
     * Handler type.
     */
    private static final Type<EnterWorkspaceEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<EnterWorkspaceEventHandler> getType() {
        return TYPE;
    }

    public EnterWorkspaceEvent(HasEnterWorkspaceData data) {
        this.person = data.getPerson();
        this.editorClientId = data.getEditorClientId();
    }

    @Override
    protected void dispatch(EnterWorkspaceEventHandler handler) {
        handler.onEnterWorkspace(this);
    }

    @Override
    public Type<EnterWorkspaceEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    public Person getPerson() {
        return person;
    }

    @Override
    public EditorClientId getEditorClientId() {
        return editorClientId;
    }
}
