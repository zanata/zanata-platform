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

import java.util.Map;

import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;

/**
 * Open Id provider interface.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface OpenIdProvider
{
   /**
    * Returns an actual open id string from a given user name.
    *
    * @param username The given user name.
    * @return An openId derived from the given user name.
    */
   String getOpenId( String username );

   /**
    * Indicates if the provider accepts this open id.
    *
    * @param openId
    * @return True if this provider accepts the open id. False otherwise.
    */
   boolean accepts( String openId );
}
