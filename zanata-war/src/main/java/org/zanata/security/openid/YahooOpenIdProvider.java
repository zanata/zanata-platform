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

/**
 * Yahoo Open Id provider.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class YahooOpenIdProvider implements OpenIdProvider
{
   private static final String YAHOO_OPENID_FORMAT = "https://me.yahoo.com/{0}";
   private static final Pattern YAHOO_OPENID_PATTERN = Pattern.compile("https://me.yahoo.com/(.*)");


   @Override
   public String getOpenId(String username)
   {
      return MessageFormat.format( YAHOO_OPENID_FORMAT, username );
   }

   @Override
   public boolean accepts(String openId)
   {
      return YAHOO_OPENID_PATTERN.matcher( openId ).matches();
   }
}
