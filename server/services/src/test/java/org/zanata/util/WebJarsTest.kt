package org.zanata.util

import org.junit.Test

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class WebJarsTest {

    @Test
    fun `can resolve all resources`() {
        WebJars().apply {
            getResource(blueimpJavaScriptTemplatesJS)
            getResource(codemirrorCSS)
            getResource(codemirrorJS)
            getResource(commonmarkJS)
            getResource(crossroadsJS)
            // TODO getResource(diffJS)
            getResource(getjQueryTypingJS())
            getResource(googleCajaHtmlSanitizerJS)
            getResource(signalsJS)
        }
    }

}
