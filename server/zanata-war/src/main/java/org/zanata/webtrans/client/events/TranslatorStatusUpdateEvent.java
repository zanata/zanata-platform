package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.HasExitWorkspaceData;
import org.zanata.webtrans.shared.rpc.HasTranslatorStatusUpdateData;
import com.google.gwt.event.shared.GwtEvent;

public class TranslatorStatusUpdateEvent extends GwtEvent<TranslatorStatusUpdateEventHandler> implements HasTranslatorStatusUpdateData
{

   private final SessionId sessionId;
   private final Person person;
   private final TransUnit selectedTransUnit;

   public TranslatorStatusUpdateEvent(HasTranslatorStatusUpdateData data)
   {
      sessionId = data.getSessionId();
      person = data.getPerson();
      selectedTransUnit = data.getSelectedTransUnit();
   }

   /**
    * Handler type.
    */
   private static Type<TranslatorStatusUpdateEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    *
    * @return returns the handler type
    */
   public static Type<TranslatorStatusUpdateEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TranslatorStatusUpdateEventHandler>();
      }
      return TYPE;
   }

   @Override
   public Type<TranslatorStatusUpdateEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(TranslatorStatusUpdateEventHandler handler)
   {
      handler.onTranslatorStatusUpdate(this);
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
