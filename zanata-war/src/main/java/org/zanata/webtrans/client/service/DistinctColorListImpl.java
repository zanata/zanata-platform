package org.zanata.webtrans.client.service;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.shared.auth.EditorClientId;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class DistinctColorListImpl implements DistinctColor
{
   private final List<String> colorList;
   private final Map<EditorClientId, String> colorMap = Maps.newHashMap();
   private int index = 0;

   @Inject
   public DistinctColorListImpl(@Named("distinctColor") List<String> colorList)
   {
      this.colorList = colorList;
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
