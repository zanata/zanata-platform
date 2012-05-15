package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitEditEvent extends GwtEvent<TransUnitEditEventHandler> implements HasTransUnitEditData
{

   private final SessionId sessionId;
   private final Person person;
   private final TransUnit selectedTransUnit;

   public TransUnitEditEvent(HasTransUnitEditData data)
   {
      sessionId = data.getSessionId();
      person = data.getPerson();
      selectedTransUnit = data.getSelectedTransUnit();
   }

   /**
    * Handler type.
    */
   private static Type<TransUnitEditEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    *
    * @return returns the handler type
    */
   public static Type<TransUnitEditEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TransUnitEditEventHandler>();
      }
      return TYPE;
   }

   @Override
   public Type<TransUnitEditEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(TransUnitEditEventHandler handler)
   {
      handler.onTransUnitEdit(this);
   }

   @Override
   public SessionId getSessionId()
   {
      return sessionId;
   }

   @Override
   public Person getPerson()
   {
      return person;
   }

   @Override
   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }
}
