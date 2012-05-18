/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.service;

import java.util.HashMap;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.user.client.Random;
import com.google.inject.Singleton;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */

@Singleton
public class UserColorService
{
   public static final HashMap<String, String> colorList = new HashMap<String, String>();

   private static final int DARK_BOUND = 100;
   private static final int BRIGHT_BOUND = 400;

   public String getColor(String sessionId)
   {

      if(colorList.containsKey(sessionId))
      {
         return colorList.get(sessionId);
      }

      String color = null;

      while (colorList.containsValue(color) || color == null || color.equals("rgb(0,0,0)") || color.equals("rgb(255,255,255)"))
      {
         color = generateNewColor();
      }

      colorList.put(sessionId, color);

      return color;
   }

   private String generateNewColor()
   {
      return generateByRGB();
   }

   private String generateByRGB()
   {
      int total = 0;

      int rndRedColor = 0;
      int rndGreenColor = 0;
      int rndBlueColor = 0;

      while (total < DARK_BOUND || total > BRIGHT_BOUND)
      {
         rndRedColor = Random.nextInt(255);
         rndGreenColor = Random.nextInt(255);
         rndBlueColor = Random.nextInt(255);
         total = rndRedColor + rndGreenColor + rndBlueColor;
      }

      CssColor randomColor = CssColor.make(rndRedColor, rndGreenColor, rndBlueColor);
      return randomColor.toString();
   }




}
