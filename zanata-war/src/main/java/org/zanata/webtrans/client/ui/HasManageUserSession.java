package org.zanata.webtrans.client.ui;

public interface HasManageUserSession
{
   void addSession(String sessionId);

   void removeSession(String sessionId);

   boolean isEmptySession();
}
