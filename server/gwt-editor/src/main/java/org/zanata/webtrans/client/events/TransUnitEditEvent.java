package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitEditEvent extends GwtEvent<TransUnitEditEventHandler>
        implements HasTransUnitEditData {

    private final EditorClientId editorClientId;
    private final Person person;
    private final TransUnitId selectedTransUnitId;

    public TransUnitEditEvent(HasTransUnitEditData data) {
        editorClientId = data.getEditorClientId();
        person = data.getPerson();
        selectedTransUnitId = data.getSelectedTransUnitId();
    }

    /**
     * Handler type.
     */
    private static final Type<TransUnitEditEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<TransUnitEditEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<TransUnitEditEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(TransUnitEditEventHandler handler) {
        handler.onTransUnitEdit(this);
    }

    @Override
    public EditorClientId getEditorClientId() {
        return editorClientId;
    }

    @Override
    public Person getPerson() {
        return person;
    }

    @Override
    public TransUnitId getSelectedTransUnitId() {
        return selectedTransUnitId;
    }

}
