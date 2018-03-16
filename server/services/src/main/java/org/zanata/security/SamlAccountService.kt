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

import org.apache.deltaspike.jpa.api.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.dao.AccountDAO
import org.zanata.dao.CredentialsDAO
import org.zanata.model.HAccount
import org.zanata.model.security.HSaml2Credentials
import org.zanata.security.annotations.SAML
import org.zanata.security.annotations.SAMLAttribute
import org.zanata.security.annotations.SAMLAttribute.AttributeName.EMAIL
import java.io.Serializable
import java.security.Principal
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

/**
 * Merge saml credentials to existing account if email matches.
 */
@RequestScoped
class SamlAccountService @Inject constructor(
        @SAMLAttribute(EMAIL) private val email: String,
        @SAML private val principal: Principal,
        private val accountDAO: AccountDAO,
        private val credentialsDAO: CredentialsDAO) : Serializable {

    private fun isFirstSignIn(uniqueId: String): Boolean = credentialsDAO.findByUser(uniqueId) == null

    @Transactional
    fun tryMergeToExistingAccount() {
        val uniqueId = principal.name
        if (isFirstSignIn(uniqueId)) {
            val hAccount: HAccount? = accountDAO.getByEmail(email)
            hAccount?.let {
                log.info("found existing account with matching email [{}]. Will reuse the account.", email)
                val credentials = HSaml2Credentials(it, uniqueId, email)
                it.credentials.add(credentials)
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SamlAccountService::class.java)
        private const val serialVersionUID: Long = 1L
    }
}
