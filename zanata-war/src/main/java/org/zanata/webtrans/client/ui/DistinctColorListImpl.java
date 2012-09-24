package org.zanata.webtrans.client.ui;

import java.util.HashMap;
import java.util.List;

import org.zanata.webtrans.shared.auth.EditorClientId;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.inject.Singleton;

/**
 * Referencing from http://eleanormaclure.files.wordpress.com/2011/03/colour-coding.pdf.
 * We store 22 visually distinct color and cycle through them.
 * If we ever get more than 22 concurrent users on one row, we will have two users share same color.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class DistinctColorListImpl implements DistinctColor
{
   private int index = 0;
   private List<String> colorList;
   private final HashMap<EditorClientId, String> colorMap;


   public DistinctColorListImpl()
   {
      // @formatter:off
      colorList = ImmutableList.<String>builder()
            .add(distinctColor(240, 163, 255))
            .add(distinctColor(0, 117, 220))
            .add(distinctColor(153, 63, 0))
            .add(distinctColor(76, 0, 92))
            .add(distinctColor(25, 25, 25))
            .add(distinctColor(0, 92, 49))
            .add(distinctColor(43, 206, 72))
            .add(distinctColor(255, 204, 153))
            .add(distinctColor(128, 128, 128))
            .add(distinctColor(148, 255, 181))
            .add(distinctColor(143, 124, 0))
            .add(distinctColor(157, 204, 0))
            .add(distinctColor(194, 0, 136))
            .add(distinctColor(0, 51, 128))
            .add(distinctColor(255, 164, 5))
            .add(distinctColor(66, 102, 0))
            .add(distinctColor(255, 0, 16))
            .add(distinctColor(94, 241, 242))
            .add(distinctColor(0, 153, 143))
            .add(distinctColor(224, 255, 102))
            .add(distinctColor(116, 10, 255))
            .add(distinctColor(153, 0, 0))
            .add(distinctColor(255, 255, 128))
            .add(distinctColor(255, 255, 0))
            .add(distinctColor(255, 80, 5))
            .build();
      // @formatter:on
      colorMap = Maps.newHashMap();
   }

   private static String distinctColor(int rndRedColor, int rndGreenColor, int rndBlueColor)
   {
      return CssColor.make(rndRedColor, rndGreenColor, rndBlueColor).value();
   }

   @Override
   public String getOrCreateColor(EditorClientId editorClientId)
   {
      if (colorMap.containsKey(editorClientId))
      {
         return colorMap.get(editorClientId);
      }

      String color = nextColor();
      colorMap.put(editorClientId, color);
      Log.info("put new color for [" + editorClientId.hashCode() + "]" + color);
      return color;
   }

   @Override
   public void releaseColor(EditorClientId editorClientId)
   {
      colorMap.remove(editorClientId);
   }

   private String nextColor()
   {
      if (index == colorList.size())
      {
         index = 0;
      }
      return colorList.get(index++);
   }

}
