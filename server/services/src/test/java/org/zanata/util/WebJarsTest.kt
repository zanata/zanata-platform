package org.zanata.util

import org.junit.Test

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class WebJarsTest {

    @Test
    fun `can resolve all resources`() {
        WebJars().apply {
            getResource(blueimpJavaScriptTemplates)
            getResource(codemirrorCSS)
            getResource(codemirrorJS)
            getResource(commonmarkJS)
            getResource(crossroadsJS)
            // TODO getResource(diffJS)
            getResource(getjQueryTypingJS())
            getResource(googleCajaHtmlSanitizerJS)
            getResource(signalsJS)

            arrayOf(
                    // NB These should match the webjars.jQueryFileUpload refs in multi-file-upload.xhtml
                    "js/cors/jquery.xdr-transport.js",
                    "js/jquery.iframe-transport.js",
                    "js/jquery.fileupload.js",
                    "js/jquery.fileupload-process.js",
                    "js/jquery.fileupload-validate.js",
                    "js/jquery.fileupload-ui.js",
                    "js/vendor/jquery.ui.widget.js"
            ).forEach {
                getResource(jQueryFileUpload(it))
            }

        }
    }

}
