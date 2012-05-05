package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.zanata.webtrans.client.ui.HasManageUserSession;

public class UserPanelSessionItem implements Serializable
{
   private static final long serialVersionUID = 1L;

   private HasManageUserSession panel;
   private ArrayList<String> sessionList;
   private TransUnit selectedTransUnit;

   public UserPanelSessionItem(HasManageUserSession panel, ArrayList<String> sessionList)
   {
      this.panel = panel;
      this.sessionList = sessionList;
   }

   public HasManageUserSession getPanel()
   {
      return panel;
   }

   public ArrayList<String> getSessionList()
   {
      if (sessionList == null)
      {
         sessionList = new ArrayList<String>();
      }
      return sessionList;
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   public void setSelectedTransUnit(TransUnit selectedTransUnit)
   {
      this.selectedTransUnit = selectedTransUnit;
   }
}
