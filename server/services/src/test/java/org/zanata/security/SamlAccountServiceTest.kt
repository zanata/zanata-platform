package org.zanata.security

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.zanata.dao.AccountDAO
import org.zanata.dao.CredentialsDAO
import org.zanata.model.HAccount
import org.zanata.model.security.HSaml2Credentials

class SamlAccountServiceTest {
    @Mock
    private lateinit var accountDAO: AccountDAO
    @Mock
    private lateinit var credentialsDAO: CredentialsDAO

    private val uniqueId = "abc-123"
    private val email = "admin@example.com"

    private lateinit var service: SamlAccountService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        service = SamlAccountService(email, SimplePrincipal(uniqueId), accountDAO, credentialsDAO)
    }

    @Test
    fun willNotMergeAccountIfItIsNotFirstSamlSignIn() {
        given(credentialsDAO.findByUser(uniqueId)).willReturn(HSaml2Credentials(HAccount(), uniqueId, email))

        service.tryMergeToExistingAccount()

        verifyZeroInteractions(accountDAO)
    }

    @Test
    fun willMergeAccountIfMatchedAccountFoundOnFirstSamlSignIn() {
        given(credentialsDAO.findByUser(uniqueId)).willReturn(null)
        val existAccount = HAccount()
        given(accountDAO.getByEmail(email)).willReturn(existAccount)

        service.tryMergeToExistingAccount()

        assertThat(existAccount.credentials).isNotEmpty
        val hCredentials = existAccount.credentials.first()
        assertThat(hCredentials).isInstanceOf(HSaml2Credentials::class.java)
        assertThat(hCredentials.account).isSameAs(existAccount)
        assertThat(hCredentials.email).isEqualTo(email)
        assertThat(hCredentials.user).isEqualTo(uniqueId)
    }
}
