package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasExitWorkspaceData;

import com.google.gwt.event.shared.GwtEvent;

public class ExitWorkspaceEvent extends GwtEvent<ExitWorkspaceEventHandler> implements HasExitWorkspaceData
{

   private final Person person;
   private final SessionId sessionId;

   /**
    * Handler type.
    */
   private static Type<ExitWorkspaceEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ExitWorkspaceEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ExitWorkspaceEventHandler>();
      }
      return TYPE;
   }

   public ExitWorkspaceEvent(HasExitWorkspaceData data)
   {
      this.person = data.getPerson();
      this.sessionId = data.getSessionId();
   }

   @Override
   protected void dispatch(ExitWorkspaceEventHandler handler)
   {
      handler.onExitWorkspace(this);
   }

   @Override
   public Type<ExitWorkspaceEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public SessionId getSessionId()
   {
      return sessionId;
   }
}
