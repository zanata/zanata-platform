package org.zanata.webtrans.shared.rpc;


public class PublishWorkspaceChat implements SessionEventData, HasWorkspaceChatData
{
   private static final long serialVersionUID = 5332535583909340461L;
   private String personId;
   private String timestamp;
   private String msg;

   public PublishWorkspaceChat(String personId, String timestamp, String msg)
   {
      this.personId = personId;
      this.timestamp = timestamp;
      this.msg = msg;
   }

   // for ExposeEntity
   public PublishWorkspaceChat()
   {
   }

   @Override
   public String getPersonId()
   {
      return personId;
   }

   @Override
   public String getTimestamp()
   {
      return timestamp;
   }

   @Override
   public String getMsg()
   {
      return msg;
   }
}