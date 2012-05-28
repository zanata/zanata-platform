package org.zanata.webtrans.client.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.Window;

public class WindowLocationImpl implements org.zanata.webtrans.client.history.Window.Location
{
   @Override
   public String getParameter(String name)
   {
      return Window.Location.getParameter(name);
   }

   @Override
   public Map<String, List<String>> getParameterMap()
   {
      return Window.Location.getParameterMap();
   }

   @Override
   public String getHref()
   {
      return Window.Location.getHref();
   }

   @Override
   public List<String> getQueryDocuments()
   {
      List<String> queryDocs = getParameterMap().get(PRE_FILTER_QUERY_PARAMETER_KEY);
      if (queryDocs != null)
      {
         // converting to ArrayList as the list type returned by the parameter
         // map does not appear to serialize properly.
         queryDocs = new ArrayList<String>(queryDocs);
      }
      return queryDocs;
   }
}