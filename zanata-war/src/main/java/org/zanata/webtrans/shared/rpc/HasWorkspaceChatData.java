package org.zanata.webtrans.shared.rpc;



public interface HasWorkspaceChatData
{

   public static enum MESSAGE_TYPE
   {
      USER_MSG, SYSTEM_MSG, SYSTEM_WARNING;
   }

   String getPersonId();

   String getTimestamp();

   String getMsg();

   MESSAGE_TYPE getMessageType();

}
