package org.zanata.action

import com.nhaarman.mockito_kotlin.given
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File
import java.lang.IllegalStateException
import javax.servlet.ServletContext

class FrontendActionTest {
    val manifestText = """
        {
  "editor.css": "editor.c6d86c21.cache.css",
  "editor.js": "editor.c6d86c21.cache.js",
  "frontend.css": "frontend.e8af8b0a.cache.css",
  "frontend.js": "frontend.e8af8b0a.cache.js",
  "frontend.legacy.css": "frontend.legacy.a31f9460.cache.css",
  "frontend.legacy.js": "frontend.legacy.a31f9460.cache.js",
  "intl-polyfill.js": "intl-polyfill.04142315.cache.js",
  "runtime.js": "runtime.e8b751b7.cache.js"
}
    """.trimIndent()

    val tempFolderRoot = File(Thread.currentThread().contextClassLoader.getResource(".")?.file)
    @Rule
    @JvmField
    val testManifestCreator: ManifestCreator = ManifestCreator(tempFolderRoot)
    @Mock
    private lateinit var servletContext: ServletContext

    private lateinit var frontendAction: FrontendAction

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        given(servletContext.contextPath).willReturn("")
    }

    @Test
    fun canGetAllFrontendJsAndCss() {
        testManifestCreator.createManifestFile(manifestText)

        frontendAction = FrontendAction(servletContext)

        assertThat(frontendAction.frontendJs).isEqualTo("/frontend.e8af8b0a.cache.js")
        assertThat(frontendAction.frontendCss).isEqualTo("/frontend.e8af8b0a.cache.css")
        assertThat(frontendAction.editorJs).isEqualTo("/editor.c6d86c21.cache.js")
        assertThat(frontendAction.editorCss).isEqualTo("/editor.c6d86c21.cache.css")
        assertThat(frontendAction.legacyJs).isEqualTo("/frontend.legacy.a31f9460.cache.js")
        assertThat(frontendAction.runtime).isEqualTo("/runtime.e8b751b7.cache.js")
    }

    @Test
    fun willThrowExceptionIfManifestFileIsNotFound() {
        assertThatThrownBy { FrontendAction(servletContext) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("can not load manifest.json from META-INF/resources/manifest.json. Did you forget to build and include zanata frontend?")
    }
}

class ManifestCreator(private val parentFolder: File) : ExternalResource() {
    override fun after() {
        super.after()
        parentFolder.deleteRecursively()
    }

    fun createManifestFile(manifestText: String) {
        val manifestParent = File(parentFolder, "META-INF/resources")
        manifestParent.mkdirs()
        val manifest = File(manifestParent, "manifest.json")
        assertThat(manifest.createNewFile()).isTrue()
        manifest.writeText(manifestText)
    }
}
