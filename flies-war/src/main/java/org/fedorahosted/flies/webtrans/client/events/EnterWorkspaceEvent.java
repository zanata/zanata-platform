package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.HasEnterWorkspaceData;

import com.google.gwt.event.shared.GwtEvent;


public class EnterWorkspaceEvent extends GwtEvent<EnterWorkspaceEventHandler> implements HasEnterWorkspaceData{

		private final PersonId personId;
		
		/**
		 * Handler type.
		 */
		private static Type<EnterWorkspaceEventHandler> TYPE;

		/**
		 * Gets the type associated with this event.
		 * 
		 * @return returns the handler type
		 */
		public static Type<EnterWorkspaceEventHandler> getType() {
			if (TYPE == null) {
				TYPE = new Type<EnterWorkspaceEventHandler>();
			}
			return TYPE;
		}
		
		public EnterWorkspaceEvent (HasEnterWorkspaceData data) {
			this.personId = data.getPersonId();
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
		public PersonId getPersonId() {
			return personId;
		}
}
