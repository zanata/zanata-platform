package org.fedorahosted.flies.hibernate.search;

import org.fedorahosted.flies.common.ContentState;
import org.hibernate.search.bridge.TwoWayStringBridge;

public class ContentStateBridge implements TwoWayStringBridge
{

   @Override
   public String objectToString(Object value)
   {
      if (value instanceof ContentState)
      {
         ContentState state = (ContentState) value;
         return state.toString();
      }
      else
      {
         throw new IllegalArgumentException("ContentStateBridge used on a non-ContentState type: " + value.getClass());
      }
   }

   @Override
   public Object stringToObject(String state)
   {
      return ContentState.valueOf(state);
   }

}
