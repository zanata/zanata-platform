package org.zanata.webtrans.client.ui;

import java.util.ArrayList;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserPanel extends HorizontalPanel implements HasManageUserSession
{
   private Image userImage;
   private Label label;
   private String name;
   private ArrayList<String> sessionIdList;

   public UserPanel(String sessionId, String name, String imgUrl)
   {
      super();
      this.name = name;

      userImage = new Image(imgUrl);
      label = new Label(name);

      sessionIdList = new ArrayList<String>();
      sessionIdList.add(sessionId);
      this.add(userImage);
      this.add(label);

      this.setCellWidth(userImage, "16px");

      updateTitle();
   }

   @Override
   public void addSession(String sessionId)
   {
      sessionIdList.add(sessionId);
      label.setText(name + "(" + sessionIdList.size() + ")");
      updateTitle();
   }

   private void updateTitle()
   {
      String title = "";
      for (String sessionId : sessionIdList)
      {
         title = Strings.isNullOrEmpty(title) ? sessionId : title + " : " + sessionId;
      }
      label.setTitle(title);
   }

   @Override
   public void removeSession(String sessionId)
   {
      sessionIdList.remove(sessionId);

      if (sessionIdList.size() == 1)
      {
         label.setText(name);
      }
      else
      {
         label.setText(name + "(" + sessionIdList.size() + ")");
      }

      updateTitle();
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
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
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

   @Override
   public boolean isEmptySession()
   {
      return sessionIdList.size() <= 0;
   }
}
