/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.security.openid;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.openid4java.message.ParameterList;

/**
 * Provider implementation for MyOpenID
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class MyOpenIdProvider extends GenericOpenIdProvider
{
   private static final String FEDORA_OPENID_FORMAT = "http://{0}.myopenid.com/";
   private static final Pattern FEDORA_OPENID_PATTERN = Pattern.compile("http://(.*).myopenid.com/");

   @Override
   public String getOpenId(String username)
   {
      return MessageFormat.format(FEDORA_OPENID_FORMAT, username);
   }

   @Override
   public boolean accepts(String openId)
   {
      return FEDORA_OPENID_PATTERN.matcher( openId ).matches();
   }

   @Override
   public String getEmail(ParameterList params)
   {
      return params.getParameterValue("openid.ax.value.email.1"); // Return the first email address
   }
}
