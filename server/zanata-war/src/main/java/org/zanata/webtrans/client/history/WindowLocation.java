/**
 * 
 */
package org.zanata.webtrans.client.history;

import java.util.List;
import java.util.Map;

/**
 * Wraps calls to the {@link com.google.gwt.user.client.Window.Location} object
 * to allow mocking for testing in a JRE (non-GWT) environment.
 * 
 * Does not implement all Window.Location methods.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public interface WindowLocation
{
   public String getParameter(String name);

   public Map<String, List<String>> getParameterMap();

   public String getHref();

}
