package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

public class PublishWorkspaceChatAction extends AbstractWorkspaceAction<PublishWorkspaceChatResult>
{
   private static final long serialVersionUID = -8145724589597122017L;
   private String person;
   private String msg;
   private MESSAGE_TYPE messageType;


   @SuppressWarnings("unused")
   private PublishWorkspaceChatAction()
   {
      this(null, null, null);
   }

   public PublishWorkspaceChatAction(String person, String msg, MESSAGE_TYPE messageType)
   {
      this.person = person;
      this.msg = msg;
      this.messageType = messageType;
   }

   public String getPerson()
   {
      return person;
   }

   public String getMsg()
   {
      return msg;
   }

   public MESSAGE_TYPE getMessageType()
   {
      return messageType;
   }
}
