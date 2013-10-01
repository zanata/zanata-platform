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

import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;

/**
 * Open Id Provider for most Open Id services.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class GenericOpenIdProvider implements OpenIdProvider {
    @Override
    public String getOpenId(String username) {
        return username; // The user name should be the open Id
    }

    @Override
    public boolean accepts(String openId) {
        return true; // Any url is a valid open Id
    }

    @Override
    public void prepareRequest(FetchRequest req) {
        try {
            // Request email
            req.addAttribute("email", "http://schema.openid.net/contact/email",
                    true);
        } catch (MessageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getEmail(ParameterList params) {
        return params.getParameterValue("openid.ext1.value.email");
    }
}
