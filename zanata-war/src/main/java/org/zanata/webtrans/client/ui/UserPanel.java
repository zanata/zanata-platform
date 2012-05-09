package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserPanel extends HorizontalPanel implements HasManageUserSession
{
   private Image userImage;
   private Label personNameLabel;
   private Label sessionLabel;
   private String personName;

   public UserPanel(String personName, String imgUrl)
   {
      super();
      this.personName = personName;

      userImage = new Image(imgUrl);
      personNameLabel = new Label(personName);
      sessionLabel = new Label();

      this.add(userImage);
      this.add(personNameLabel);
      this.add(sessionLabel);

      this.setCellWidth(userImage, "16px");
   }

   @Override
   public void updateTitle(String title)
   {
      personNameLabel.setTitle(title);
   }

   @Override
   public void updateSessionLabel(String session)
   {
      sessionLabel.setText(session);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((personName == null) ? 0 : personName.hashCode());
      result = prime * result + ((userImage == null) ? 0 : userImage.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      UserPanel other = (UserPanel) obj;
      if (personName == null)
      {
         if (other.personName != null)
            return false;
      }
      else if (!personName.equals(other.personName))
         return false;
      if (userImage == null)
      {
         if (other.userImage != null)
            return false;
      }
      else if (!userImage.equals(other.userImage))
         return false;
      return true;
   }
}
