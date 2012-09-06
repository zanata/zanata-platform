package org.zanata.webtrans.shared.rpc;

import java.util.logging.Level;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class RemoteLoggingAction extends AbstractWorkspaceAction<NoOpResult>
{
   private String message;

   // default log to severe level
   private Level logLevel = Level.SEVERE;

   @SuppressWarnings("unused")
   public RemoteLoggingAction()
   {
   }

   public RemoteLoggingAction(String message)
   {
      this.message = message;
   }

   public String getMessage()
   {
      return message;
   }

   public Level getLogLevel()
   {
      return logLevel;
   }

   /**
    * We only log Severe, Warning or Info in the handler.
    * @see org.zanata.webtrans.server.rpc.RemoteLoggingHandler
    * @param logLevel log level
    */
   public void setLogLevel(Level logLevel)
   {
      this.logLevel = logLevel;
   }
}
