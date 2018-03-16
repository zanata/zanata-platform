package org.zanata.servlet

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import javax.servlet.ServletContext

class UrlRewriteConfigTest {
    private lateinit var urlRewriteConfig: UrlRewriteConfig
    @Mock
    private lateinit var context: ServletContext

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        urlRewriteConfig = UrlRewriteConfig()
    }


    @Test
    fun hasConfigForSingleOpenIdProvider() {
        val configuration = urlRewriteConfig.getConfiguration(context)
        val rulesStrings = configuration.rules.map { it.toString() }
        val result = rulesStrings.filter { it.contains("/account/singleopenidlogin") }
        Assertions.assertThat(result).hasSize(1)
    }
}
