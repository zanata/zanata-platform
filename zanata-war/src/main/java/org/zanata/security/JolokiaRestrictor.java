/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.security;

import lombok.extern.slf4j.Slf4j;
import org.jolokia.restrictor.Restrictor;
import org.jolokia.util.HttpMethod;
import org.jolokia.util.RequestType;
import org.zanata.util.ServiceLocator;

import javax.management.ObjectName;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Slf4j
public class JolokiaRestrictor implements Restrictor {

    private boolean checkAdmin() {
        try {
            return ServiceLocator.instance().getInstance(ZanataIdentity.class).hasRole("admin");
        } catch (Exception e) {
            log.debug("security check failed", e);
            return false;
        }
    }
    @Override
    public boolean isHttpMethodAllowed(HttpMethod pMethod) {
        return checkAdmin();
    }

    @Override
    public boolean isTypeAllowed(RequestType pType) {
        return checkAdmin();
    }

    @Override
    public boolean isAttributeReadAllowed(ObjectName pName, String pAttribute) {
        return checkAdmin();
    }

    @Override
    public boolean isAttributeWriteAllowed(ObjectName pName,
            String pAttribute) {
        return checkAdmin();
    }

    @Override
    public boolean isOperationAllowed(ObjectName pName, String pOperation) {
        return checkAdmin();
    }

    @Override
    public boolean isRemoteAccessAllowed(String... pHostOrAddress) {
        return checkAdmin();
    }

    @Override
    public boolean isOriginAllowed(String pOrigin, boolean pIsStrictCheck) {
        return checkAdmin();
    }
}
