package org.zanata.webtrans.shared.model;

import org.zanata.webtrans.client.ui.HasManageUserPanel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserPanelSessionItem implements IsSerializable
{
   private HasManageUserPanel panel;
   private Person person;
   private TransUnitId selectedId;

   public UserPanelSessionItem(HasManageUserPanel panel, Person person)
   {
      this.panel = panel;
      this.person = person;
   }

   @SuppressWarnings("unused")
   protected UserPanelSessionItem()
   {
   }

   public HasManageUserPanel getPanel()
   {
      return panel;
   }

   public Person getPerson()
   {
      return person;
   }

   public TransUnitId getSelectedId()
   {
      return selectedId;
   }

   public void setSelectedId(TransUnitId selectedId)
   {
      this.selectedId = selectedId;
   }
}
