package net.openl10n.flies.webtrans.client.events;

import net.openl10n.flies.webtrans.shared.model.PersonId;
import net.openl10n.flies.webtrans.shared.rpc.HasEnterWorkspaceData;

import com.google.gwt.event.shared.GwtEvent;

public class EnterWorkspaceEvent extends GwtEvent<EnterWorkspaceEventHandler> implements HasEnterWorkspaceData
{

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
   public static Type<EnterWorkspaceEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<EnterWorkspaceEventHandler>();
      }
      return TYPE;
   }

   public EnterWorkspaceEvent(HasEnterWorkspaceData data)
   {
      this.personId = data.getPersonId();
   }

   @Override
   protected void dispatch(EnterWorkspaceEventHandler handler)
   {
      handler.onEnterWorkspace(this);
   }

   @Override
   public Type<EnterWorkspaceEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   public PersonId getPersonId()
   {
      return personId;
   }
}
