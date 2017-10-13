/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.security

import io.undertow.security.idm.Account
import org.apache.deltaspike.core.api.common.DeltaSpike
import org.picketlink.common.constants.GeneralConstants.SESSION_ATTRIBUTE_MAP
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE
import org.zanata.security.annotations.SAML
import org.zanata.security.annotations.SAMLAttribute
import org.zanata.security.annotations.SAMLAttribute.AttributeName.UID
import org.zanata.security.annotations.SAMLAttribute.AttributeName.CN
import org.zanata.security.annotations.SAMLAttribute.AttributeName.EMAIL
import java.security.Principal
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.servlet.http.HttpSession

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
open class SamlAttributes @Inject constructor(@DeltaSpike private val session: HttpSession) {

    open val isSessionAuthenticatedBySAML get() = principalFromSAMLResponse() != null

    @Suppress("UNCHECKED_CAST")
    private val attributeMap: Map<String, List<String>> =
            session.getAttribute(SESSION_ATTRIBUTE_MAP) as? Map<String, List<String>> ?: mapOf()

    @Produces
    @SAML
    fun principalFromSAMLResponse(): Principal? {
        val account = session.getAttribute(FORM_ACCOUNT_NOTE) as? Account
        if (account != null && account.roles.contains("authenticated")) {
            return account.principal
        }
        return null
    }

    @Produces
    @SAMLAttribute(UID)
    fun usernameFromSAMLResponse(@SAML principal: Principal?): String? {
        if (principal == null) return null
        val principalName = principal.name
        // In some IDPs, this may be a more readable username than principal name.
        return getValueFromSessionAttribute(attributeMap, UID.key, defaultVal = principalName)
    }

    @Produces
    @SAMLAttribute(CN)
    fun commonNameFromSAMLResponse(@SAML principal: Principal?): String? {
        if (principal == null) return null
        return getValueFromSessionAttribute(attributeMap, CN.key)
    }

    @Produces
    @SAMLAttribute(EMAIL)
    fun emailFromSAMLResponse(@SAML principal: Principal?): String? {
        if (principal == null) return null
        return getValueFromSessionAttribute(attributeMap, EMAIL.key)
    }

    companion object {
        private fun getValueFromSessionAttribute(map: Map<String, List<String>?>, key: String, defaultVal: String = "") =
                map[key]?.elementAtOrNull(0) ?: defaultVal

    }
}
