package org.zanata.dao

import com.nhaarman.mockitokotlin2.given
import org.assertj.core.api.Assertions
import org.hibernate.Session
import org.jglue.cdiunit.InRequestScope
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.zanata.ZanataJpaTest
import org.zanata.model.HAccount
import org.zanata.model.security.HOpenIdCredentials
import org.zanata.model.security.HSaml2Credentials
import org.zanata.model.validator.UniqueValidator
import org.zanata.test.CdiUnitRunner
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.validation.ConstraintValidatorContext

@RunWith(CdiUnitRunner::class)
@SupportDeltaspikeCore
class CredentialsDAOTest : ZanataJpaTest() {

    @Produces @Mock
    private lateinit var uniqueValidator: UniqueValidator
    @Produces
    fun produceSession(): Session = session

    @Inject
    private lateinit var credentialsDAO: CredentialsDAO

    @Test
    @InRequestScope
    fun canLookupHSaml2Credentials() {
        val hAccount = HAccount()
        hAccount.username = "admin"
        getEm().persist(hAccount)

        given(uniqueValidator.isValid(any(), any(ConstraintValidatorContext::class.java))).willReturn(true)
        val credentials = HSaml2Credentials(hAccount, "abc-123", "admin@example.com")
        getEm().persist(credentials)

        val openIdCredentials = HOpenIdCredentials(hAccount, "https://example.com", "admin@example.com")
        getEm().persist(openIdCredentials)

        val result = credentialsDAO.findByUser("abc-123")

        Assertions.assertThat(result).isEqualTo(credentials)
    }

}
