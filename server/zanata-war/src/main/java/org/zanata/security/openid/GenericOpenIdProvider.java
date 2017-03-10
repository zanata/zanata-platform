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

import com.google.common.collect.Sets;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.sreg.SRegMessage;
import javax.enterprise.inject.Alternative;
import java.util.Collection;
import java.util.HashSet;

/**
 * Open Id Provider for most Open Id services.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Alternative
public class GenericOpenIdProvider implements OpenIdProvider {

    private boolean sregEnabled = true;
    private boolean aexEnabled = true;

    @Override
    public String getOpenId(String username) {
        return username; // The user name should be the open Id
    }

    @Override
    public boolean accepts(String openId) {
        return true; // Any url is a valid open Id
    }

    @Override
    public Collection<MessageExtension> createExtensions() {
        HashSet<MessageExtension> extensions = Sets.newHashSet();
        if (aexEnabled) {
            AxMessage aexExt = new AxMessage();
            aexExt.getParameters().set(new Parameter("mode", "fetch_request"));
            aexExt.getParameters()
                    .set(new Parameter("required", "email,fullname,nickname"));
            aexExt.getParameters().set(new Parameter("type.email",
                    "http://axschema.org/contact/email"));
            aexExt.getParameters().set(new Parameter("type.fullname",
                    "http://axschema.org/namePerson"));
            aexExt.getParameters().set(new Parameter("type.nickname",
                    "http://axschema.org/namePerson/friendly"));
            extensions.add(aexExt);
        }
        if (sregEnabled) {
            SRegMessage sregExt = new SRegMessage();
            // We use the 1.1 SREG spec
            sregExt.setTypeUri(SRegMessage.OPENID_NS_SREG11);
            sregExt.getParameters()
                    .set(new Parameter("required", "email,fullname,nickname"));
            extensions.add(sregExt);
        }
        return extensions;
    }

    @Override
    public String getAliasForExtension(MessageExtension ext) {
        if (ext instanceof AxMessage) {
            return "ax";
        } else if (ext instanceof SRegMessage) {
            return "sreg";
        }
        return null;
    }

    private String getAttExchangeParameter(AuthSuccess authSuccess,
            String parameterName) {
        String paramValue = null;
        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
            // Get email from different possible parameters (single or multiple)
            paramValue = authSuccess
                    .getParameterValue("openid.ext1.value." + parameterName);
            if (paramValue == null) {
                paramValue = authSuccess.getParameterValue(
                        "openid.ax.value." + parameterName + ".1");
            }
        }
        return paramValue;
    }

    private String getSregParameter(AuthSuccess authSuccess,
            String parameterName) {
        String paramValue = null;
        if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG11)) {
            paramValue = authSuccess
                    .getParameterValue("openid.sreg." + parameterName);
        }
        return paramValue;
    }

    @Override
    public String getEmail(AuthSuccess authSuccess) {
        String email = null;
        if (aexEnabled) {
            email = getAttExchangeParameter(authSuccess, "email");
        }
        if (email == null && sregEnabled) {
            email = getSregParameter(authSuccess, "email");
        }
        return email;
    }

    @Override
    public String getUsername(AuthSuccess authSuccess) {
        String username = null;
        if (aexEnabled) {
            username = getAttExchangeParameter(authSuccess, "nickname");
        }
        if (username == null && sregEnabled) {
            username = getSregParameter(authSuccess, "nickname");
        }
        return username;
    }

    @Override
    public String getFullName(AuthSuccess authSuccess) {
        String fullName = null;
        if (aexEnabled) {
            fullName = getAttExchangeParameter(authSuccess, "fullname");
        }
        if (fullName == null && sregEnabled) {
            fullName = getSregParameter(authSuccess, "fullname");
        }
        return fullName;
    }

    protected void setSregEnabled(final boolean sregEnabled) {
        this.sregEnabled = sregEnabled;
    }

    protected boolean isSregEnabled() {
        return this.sregEnabled;
    }

    protected void setAexEnabled(final boolean aexEnabled) {
        this.aexEnabled = aexEnabled;
    }

    protected boolean isAexEnabled() {
        return this.aexEnabled;
    }
}
