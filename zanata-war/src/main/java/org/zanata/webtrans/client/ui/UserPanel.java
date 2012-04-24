package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserPanel extends HorizontalPanel
{
   private final String id;
   private int sessionCount = 1;

   private Image userImage;
   private Label label;
   private String name;

   public UserPanel(String id, String name, String imgUrl)
   {
      super();
      this.id = id;
      this.name = name;

      userImage = new Image(imgUrl);
      label = new Label(name);

      this.add(userImage);
      this.add(label);

      this.setCellWidth(userImage, "16px");
   }

   public String getId()
   {
      return id;
   }

   public boolean isSingleSession()
   {
      return sessionCount == 1;
   }

   public void addSession()
   {
      sessionCount++;
      label.setText(name + "(" + sessionCount + ")");
   }

   public void removeSession()
   {
      sessionCount--;
      sessionCount = sessionCount < 1 ? 1 : sessionCount;

      if (sessionCount == 1)
      {
         label.setText(name);
      }
      else
      {
         label.setText(name + "(" + sessionCount + ")");
      }
   }
}
