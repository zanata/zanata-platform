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
package org.zanata.adapter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.apache.commons.io.output.ByteArrayOutputStream
import org.junit.Before
import org.junit.Test
import org.zanata.adapter.FileFormatAdapter.ParserOptions
import org.zanata.adapter.FileFormatAdapter.WriterOptions
import org.zanata.common.ContentState
import org.zanata.common.dto.TranslatedDoc
import org.zanata.exception.FileFormatAdapterException
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.rest.dto.resource.TranslationsResource
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import org.zanata.common.LocaleId.EN
import org.zanata.common.LocaleId.FR
import java.io.FileOutputStream

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class JsonAdapterTest : AbstractAdapterTest<JsonAdapter>() {

    @Before
    fun setup() {
        adapter = JsonAdapter()
    }

    private fun <T> withTempFile(content: String, parser: (File) -> T): T {
        val file: File = createTempFile()
        try {
            file.writeText(content)
            return parser.invoke(file)
        } finally {
            file.delete()
        }
    }

    @Test
    fun parseABasicJSONWithArray() {
        val resource: Resource = withTempFile("""
            {
              "test": {
                "title": "Line One",
                "test1": {
                  "title": "Line Two",
                  "test2": {
                    "test3": {
                      "ID": "Line Three",
                      "testarray": ["First", "Second"]
                    }
                  }
                }
              }
            }
            """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(5)
        assertThat(resource.textFlows[0].contents).containsExactly("Line One")
        assertThat(resource.textFlows[1].contents).containsExactly("Line Two")
        assertThat(resource.textFlows[2].contents).containsExactly("Line Three")
        assertThat(resource.textFlows[3].contents).containsExactly("First")
        assertThat(resource.textFlows[4].contents).containsExactly("Second")
    }

    /*
     * JSON with similar keys at the same level is not valid, but will not
     * prevent parsing of valid content
     */
    @Test
    fun duplicateKeysAreOverwritten() {
        val resource: Resource = withTempFile("""
            {
              "test": {
                "title": "Line One",
                "test1": {
                  "title": "Line Two"
                },
                "test1": {
                  "title": "Line Three"
                }
              }
            }
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows[0].contents).containsExactly("Line One")
        assertThat(resource.textFlows[1].contents).containsExactly("Line Three")
    }

    /*
     * JSON parts with similar content are separate
     */
    @Test
    fun duplicateContentIsAccepted() {
        val resource: Resource = withTempFile("""
            {
              "test": {
                "title": "Dupe",
                "test1": {
                  "title": "Same"
                },
                "test2": {
                  "differenttitle": "Same"
                }
              }
            }
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(3)
        assertThat(resource.textFlows[0].contents).containsExactly("Dupe")
        assertThat(resource.textFlows[1].contents).containsExactly("Same")
        assertThat(resource.textFlows[2].contents).containsExactly("Same")
    }

    @Test
    fun parseTranslatedJsonFile() {
        withTempFile("""
            {
              "test": {
                "title": "Test",
                "test1": {
                  "title": "Foun’dé metalkcta"
                },
                "test2": {
                  "title": "Tba’dé metalkcta"
                },
                "test3": {
                  "title": "Kba’dé metalkcta"
                }
              }
            }
        """.trimIndent()) { transFile ->
            val transParser = ParserOptions(transFile.toURI(), FR, "")
            val translationsResource = adapter.parseTranslationFile(transParser)
            assertThat(translationsResource.textFlowTargets).hasSize(4)
            assertThat(translationsResource.textFlowTargets[0]
                    .contents[0]).isEqualTo("Test")
            assertThat(translationsResource.textFlowTargets[1]
                    .contents[0]).isEqualTo("Foun’dé metalkcta")
            assertThat(translationsResource.textFlowTargets[2]
                    .contents[0]).isEqualTo("Tba’dé metalkcta")
            assertThat(translationsResource.textFlowTargets[3]
                    .contents[0]).isEqualTo("Kba’dé metalkcta")
        }
    }

    @Test
    fun translatedEntriesAreCopiedWhenNotApprovedOnly() {
        testTranslatedJSONDocument(false)
    }

    @Test
    fun onlyApprovedEntriesAreCopiedWhenApprovedOnly() {
        testTranslatedJSONDocument(true)
    }

    private fun testTranslatedJSONDocument(approvedOnly: Boolean) {
        withTempFile("""
                {
                  "test": {
                    "title": "Test",
                    "test1": {
                      "title": "First Source"
                    },
                    "test2": {
                      "title": "Second Source"
                    },
                    "test3": {
                      "title": "Third Source"
                    }
                  }
                }""".trimIndent()) { originalFile ->
            val resource = parseTestFile(originalFile)
            assertThat(resource.textFlows[1].contents).containsExactly("First Source")
            assertThat(resource.textFlows[2].contents).containsExactly("Second Source")
            assertThat(resource.textFlows[3].contents).containsExactly("Third Source")

            val transResource = TranslationsResource()
            transResource.textFlowTargets.addAll(listOf(
                    TextFlowTarget().apply {
                        resId = "test.test1.title"
                        contents = listOf("Foun’dé metalkcta")
                        state = ContentState.Approved
                    },
                    TextFlowTarget().apply {
                        resId = "test.test2.title"
                        contents = listOf("Tba’dé metalkcta")
                        state = ContentState.Translated
                    },
                    TextFlowTarget().apply {
                        resId = "test.test3.title"
                        contents = listOf("Kba’dé metalkcta")
                        state = ContentState.NeedReview
                    }))

            val outputStream = ByteArrayOutputStream()
            val sourceOptions = ParserOptions(originalFile.toURI(), EN, "")
            val translatedDoc = TranslatedDoc(resource, transResource, FR)
            val options = WriterOptions(sourceOptions, translatedDoc)
            adapter.writeTranslatedFile(outputStream, options, approvedOnly)

            val firstTitle = "Foun’dé metalkcta"
            val secondTitle = if (approvedOnly) "Second Source" else "Tba’dé metalkcta"
            val thirdTitle = "Third Source"

            assertThat(outputStream.toString(UTF_8)).isEqualTo("""
            {
              "test": {
                "title": "Test",
                "test1": {
                  "title": "$firstTitle"
                },
                "test2": {
                  "title": "$secondTitle"
                },
                "test3": {
                  "title": "$thirdTitle"
                }
              }
            }""".trimIndent())

        }

    }

    @Test
    fun handleDotInKey() {
        val resource: Resource = withTempFile("""
            {
              "test": {
                "title": "First source",
                "test.1": {
                  "title": "Second source"
                },
                "test": {
                  "1": {
                    "title": "Third source"
                  }
                }
              }
            }
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(3)
        assertThat(resource.textFlows[0].contents).containsExactly("First source")
        assertThat(resource.textFlows[1].contents).containsExactly("Second source")
        assertThat(resource.textFlows[2].contents).containsExactly("Third source")
    }

    @Test
    fun handleNumberAsValue() {
        val resource: Resource = withTempFile("""
            {
              "test": {
                "title": "Number 1",
                "test1": {
                  "title": 2
                },
                "test2": {
                  "title": "Number Three"
                }
              }
            }
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(2)
        assertThat(resource.textFlows[0].contents).containsExactly("Number 1")
        assertThat(resource.textFlows[1].contents).containsExactly("Number Three")
    }

    @Test
    fun failGracefullyWhenNoOriginalFile() {
        val sourceOptions = ParserOptions(File("non-exist.json").toURI(), EN, "")
        val translatedDoc = TranslatedDoc(Resource(), TranslationsResource(), FR)
        try {
            val options = WriterOptions(sourceOptions, translatedDoc)
            adapter.writeTranslatedFile(ByteArrayOutputStream(), options, false)
            fail("Expected a FileFormatAdapterException")
        } catch (ffae: FileFormatAdapterException) {
            assertThat(ffae.message)
                    .isEqualTo("Cannot open the source file")
        }
    }

    @Test
    fun failGracefullyWhenOutputIsNotWritable() {
        withTempFile("""
                {
                  "test": "Test"
                }""".trimIndent()) { originalFile ->
            val resource: Resource = parseTestFile(originalFile)
            val sourceOptions = ParserOptions(originalFile.toURI(), EN, "")
            val transResource = TranslationsResource()
            transResource.textFlowTargets.addAll(listOf(
                    TextFlowTarget().apply {
                        resId = "test"
                        contents = listOf("Foun’dé metalkcta")
                        state = ContentState.Approved
                    }))
            val translatedDoc = TranslatedDoc(resource, transResource, FR)
            val fileOutputStream = FileOutputStream(createTempFile("bad-file.json"))
            fileOutputStream.close()
            try {
                val options = WriterOptions(sourceOptions, translatedDoc)
                adapter.writeTranslatedFile(fileOutputStream, options, false)
                fail("Expected a FileFormatAdapterException")
            } catch (ffae: FileFormatAdapterException) {
                assertThat(ffae.message)
                        .isEqualTo("Cannot create the translated file")
            }
        }
    }

}
