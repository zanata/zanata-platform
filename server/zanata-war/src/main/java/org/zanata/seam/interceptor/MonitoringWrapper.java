/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.seam.interceptor;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.zanata.security.ZanataIdentity;

import net.bull.javamelody.MonitoringFilter;

public class MonitoringWrapper extends MonitoringFilter
{

   @Override
   public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      final HttpServletResponse httpResponse = (HttpServletResponse) response;

      if (httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest)))
      {
         new ContextualHttpServletRequest((HttpServletRequest) request)
         {
            @Override
            public void process() throws Exception
            {
               ZanataIdentity identity = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
               if (identity == null || !identity.isLoggedIn())
               {
                  String signInUrl = httpRequest.getContextPath() + "/account/sign_form";
                  httpResponse.sendRedirect(signInUrl);
               }
               else if (!identity.hasRole("admin"))
               {
                  httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Only admin can access monitoring!");
               }
            }
         }.run();
         super.doFilter(request, response, chain);
      }
      else
      {
         chain.doFilter(request, response);
      }
   }

}
