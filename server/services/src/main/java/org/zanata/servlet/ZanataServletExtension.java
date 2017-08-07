/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.servlet;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.util.ImmediateAuthenticationMechanismFactory;
import org.zanata.security.DummyAuthenticationMechanism;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;

/**
 * This Undertow servlet extension adds a dummy SPNEGO implementation so that we
 * can deploy on WildFly, and also provides other workarounds as needed.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ZanataServletExtension implements ServletExtension {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ZanataServletExtension.class);

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo,
            ServletContext servletContext) {
        if (!deploymentInfo.getAuthenticationMechanisms()
                .containsKey("SPNEGO")) {
            log.debug("Registering dummy SPNEGO authentication mechanism");
            deploymentInfo.addAuthenticationMechanism("SPNEGO",
                    new ImmediateAuthenticationMechanismFactory(
                            new DummyAuthenticationMechanism()));
        }
        String contextPath = servletContext.getContextPath();
        // workaround for https://issues.jboss.org/browse/WFLY-3744
        if (contextPath == null || contextPath.equals("/")) {
            log.warn(
                    "ContextPath was \"/\", changing to \"\" (WFLY-3744 workaround)");
            deploymentInfo.setContextPath("");
        }
        // workaround for https://issues.jboss.org/browse/WFLY-3617
        SessionCookieConfig cookieConfig =
                servletContext.getSessionCookieConfig();
        String cookiePath = cookieConfig.getPath();
        if (cookiePath == null) {
            log.info("Cookie path is null");
        } else if (cookiePath.isEmpty()) {
            String newCookiePath;
            if (contextPath == null || contextPath.isEmpty()) {
                newCookiePath = "/";
            } else {
                newCookiePath = contextPath;
            }
            log.warn(
                    "Cookie path was empty, changing to \"{}\" (WFLY-3617 workaround)",
                    newCookiePath);
            cookieConfig.setPath(newCookiePath);
        } else {
            log.info("Cookie path is \"{}\"", cookiePath);
        }
    }
}
