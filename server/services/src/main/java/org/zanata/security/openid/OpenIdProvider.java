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

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;

import java.io.Serializable;
import java.util.Collection;

/**
 * Open Id provider interface.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface OpenIdProvider extends Serializable {
    /**
     * Returns an actual open id string from a given user name.
     *
     * @param username
     *            The given user name.
     * @return An openId derived from the given user name.
     */
    String getOpenId(String username);

    /**
     * Indicates if the provider accepts this open id.
     *
     * @param openId
     * @return True if this provider accepts the open id. False otherwise.
     */
    boolean accepts(String openId);

    /**
     * Returns the extensions that the openid provider supports.
     * @return A collection of extensions to be used in an openid authentication
     * request.
     */
    Collection<MessageExtension> createExtensions();

    /**
     * Returns an alias to use for the provided message extension.
     * @param ext The extension message to send with the auth request.
     * @return The alias to use for the extension. Or null if it can't be
     * determined.
     */
    String getAliasForExtension(MessageExtension ext);

    /**
     * Returns the email address as returned by the open Id provider upon
     * authentication.
     *
     * @param authSuccess
     *            Authentication success validated by the open id provider.
     * @return Email address if returned by the open id provider. If not, null.
     */
    String getEmail(AuthSuccess authSuccess);

    /**
     * Returns the user name as returned by the open Id provider upon
     * authentication.
     *
     * @param authSuccess
     *            Authentication success validated by the open id provider.
     * @return User name if returned by the open id provider. If not, null.
     */
    String getUsername(AuthSuccess authSuccess);

    /**
     * Returns the Full Name as returned by the open Id provider upon
     * authentication.
     *
     * @param authSuccess
     *            Authentication success validated by the open id provider.
     * @return User's full name if returned by the open id provider.
     * If not, null.
     */
    String getFullName(AuthSuccess authSuccess);
}
