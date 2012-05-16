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

import com.google.gwt.user.client.Random;
import com.google.inject.Singleton;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */

@Singleton
public class TranslatorColorService
{
   public static final HashMap<String, String> colorList = new HashMap<String, String>();
   
   private static final String[] HEX_LIST = new String[] { "0", "1", "2", "3", "4", "5", "6", "7",
      "8", "9", "A", "B", "C", "D", "E", "F" };

   public String getColor(String sessionId)
   {

      if(colorList.containsKey(sessionId))
      {
         return colorList.get(sessionId);
      }

      String color = null;

      while (colorList.containsValue(color) || color == null || color.equals("#FFFFFF") || color.equals("#000000"))
      {
         color = generateNewColor();
      }

      colorList.put(sessionId, color);

      return color;
   }

   private String generateNewColor()
   {
      String hex1 = getRandomHex();
      String hex2 = getRandomHex();
      String hex3 = getRandomHex();
      String hex4 = getRandomHex();
      String hex5 = getRandomHex();
      String hex6 = getRandomHex();

      String color = "#" + hex1 + hex2 + hex3 + hex4 + hex5 + hex6;
      return color;
   }

   private static String getRandomHex() {
      int randomNum = Random.nextInt(HEX_LIST.length);
      String sHex = HEX_LIST[randomNum];
      return sHex;
   }
}
