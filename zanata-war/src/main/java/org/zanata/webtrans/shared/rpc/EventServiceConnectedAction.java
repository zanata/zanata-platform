package org.zanata.webtrans.shared.rpc;


public class EventServiceConnectedAction extends AbstractWorkspaceAction<NoOpResult>
{

   private static final long serialVersionUID = 1L;
   private String connectionId;

   @SuppressWarnings("unused")
   private EventServiceConnectedAction()
   {
   }

   public EventServiceConnectedAction(String connectionId)
   {
      this.connectionId = connectionId;
   }

   public String getConnectionId()
   {
      return connectionId;
   }

}
