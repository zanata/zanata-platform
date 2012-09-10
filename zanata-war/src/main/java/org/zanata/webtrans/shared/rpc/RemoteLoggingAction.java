package org.zanata.webtrans.shared.rpc;

import java.util.logging.Level;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RemoteLoggingAction extends AbstractWorkspaceAction<NoOpResult>
{
   private String message;

   @SuppressWarnings("unused")
   public RemoteLoggingAction()
   {
   }

   /**
    * We only log as ERROR in the handler.
    *
    * @param message log message
    * @see org.zanata.webtrans.server.rpc.RemoteLoggingHandler
    */
   public RemoteLoggingAction(String message)
   {
      this.message = message;
   }

   public String getMessage()
   {
      return message;
   }
}
