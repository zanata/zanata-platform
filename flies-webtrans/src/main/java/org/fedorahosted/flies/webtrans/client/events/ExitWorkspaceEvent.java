package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.TransUnitId;
import org.fedorahosted.flies.gwt.rpc.HasExitWorkspaceData;
import org.fedorahosted.flies.gwt.rpc.HasTransUnitUpdatedData;

import com.google.gwt.event.shared.GwtEvent.Type;

public class ExitWorkspaceEvent extends SequenceEvent<ExitWorkspaceEventHandler> {

	private final PersonId personId;
	
	/**
	 * Handler type.
	 */
	private static Type<ExitWorkspaceEventHandler> TYPE;

	/**
	 * Gets the type associated with this event.
	 * 
	 * @return returns the handler type
	 */
	public static Type<ExitWorkspaceEventHandler> getType() {
		if (TYPE == null) {
			TYPE = new Type<ExitWorkspaceEventHandler>();
		}
		return TYPE;
	}
	
	public ExitWorkspaceEvent (HasExitWorkspaceData data, int sequence) {
		super(sequence);
		this.personId = data.getPersonId();
	}

	@Override
	protected void dispatch(ExitWorkspaceEventHandler handler) {
		handler.onExitWorkspace(this);
	}

	@Override
	public Type<ExitWorkspaceEventHandler> getAssociatedType() {
		// TODO Auto-generated method stub
		return getType();
	}

	public PersonId getPersonId() {
		return personId;
	}
	


}
