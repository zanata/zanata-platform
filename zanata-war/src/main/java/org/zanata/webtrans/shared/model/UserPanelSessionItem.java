package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import org.zanata.webtrans.client.ui.HasManageUserPanel;

public class UserPanelSessionItem implements Serializable
{
   private static final long serialVersionUID = 1L;

   private HasManageUserPanel panel;
   private Person person;
   private TransUnit selectedTransUnit;

   public UserPanelSessionItem(HasManageUserPanel panel, Person person)
   {
      this.panel = panel;
      this.person = person;
   }

   public HasManageUserPanel getPanel()
   {
      return panel;
   }

   public Person getPerson()
   {
      return person;
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
