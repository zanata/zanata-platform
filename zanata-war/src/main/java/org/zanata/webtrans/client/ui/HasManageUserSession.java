package org.zanata.webtrans.client.ui;

import java.io.Serializable;

public interface HasManageUserSession extends Serializable
{
   void updateTitle(String title);

   void updateSessionLabel(String session);

   void addColor(String color);

   void setColor(String color);

   void clearColorList();
}
