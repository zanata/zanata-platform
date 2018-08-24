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

import java.util.HashMap

import com.google.common.base.Charsets
import net.sf.okapi.common.Event
import net.sf.okapi.common.EventType
import net.sf.okapi.common.LocaleId
import net.sf.okapi.common.resource.DocumentPart
import net.sf.okapi.common.resource.RawDocument

import net.sf.okapi.common.resource.StartDocument
import net.sf.okapi.common.skeleton.GenericSkeleton
import net.sf.okapi.filters.ts.TsFilter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.output.ByteArrayOutputStream
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.zanata.adapter.FileFormatAdapter.ParserOptions
import org.zanata.adapter.FileFormatAdapter.WriterOptions
import org.zanata.common.ContentState
import org.zanata.common.ContentType
import org.zanata.common.DocumentType
import org.zanata.common.LocaleId.EN
import org.zanata.exception.FileFormatAdapterException
import org.zanata.model.HDocument
import org.zanata.model.HLocale
import org.zanata.model.HRawDocument
import org.zanata.rest.dto.extensions.comment.SimpleComment
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TextFlow
import org.zanata.rest.dto.resource.TextFlowTarget
import java.io.File
import org.zanata.common.LocaleId.FR
import org.zanata.common.dto.TranslatedDoc
import org.zanata.rest.dto.resource.TranslationsResource
import org.zanata.util.OkapiUtil.toOkapiLocale

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TSAdapterTest : AbstractAdapterTest<TSAdapter>() {

    @Before
    fun setup() {
        adapter = TSAdapter()
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
    fun parseTS() {
        val resource: Resource = withTempFile("""
            <!DOCTYPE TS []>
            <TS>
              <context>
                <name>Test</name>
                  <message>
                    <source>Line One</source>
                    <translation>Teststring1</translation>
                  </message>
                  <message>
                  <source>Line Two</source>
                  <translation>Teststring2</translation>
                  </message>
                  <message>
                  <source>Line Three</source>
                  <translation>Teststring3</translation>
                  </message>
              </context>
            </TS>
        """.trimIndent(), this::parseTestFile)
        assertThat(getContext(resource.textFlows[0])).isEqualTo("Test")
        assertThat(resource.textFlows).hasSize(3)
        assertThat(resource.textFlows[0].contents).containsExactly("Line One")
        assertThat(resource.textFlows[1].contents).containsExactly("Line Two")
        assertThat(resource.textFlows[2].contents).containsExactly("Line Three")
    }

    /*
     * TS plural sets source textflow to have same content for number of plurals
     */
    @Test
    fun testTSWithPlurals() {
        val resource: Resource = withTempFile("""
            <!DOCTYPE TS []>
            <TS version="2.1" sourcelanguage="en" language="sv">
              <context>
                <name>testContext</name>
                <message numerus="yes">
                  <source>%1 takes at most %n argument(s). %2 is therefore invalid.</source>
                  <translation>
                    <numerusform>%1 prend au maximum %n argument. %2 est donc invalide.</numerusform>
                    <numerusform>%1 prend au maximum %n arguments. %2 est donc invalide.</numerusform>
                  </translation>
                </message>
              </context>
            </TS>
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(1)
        assertThat(resource.textFlows[0].isPlural).isTrue()
        assertThat(resource.textFlows[0].contents.size).isEqualTo(2)
    }

    @Test
    fun testTSWithMultipleContexts() {
        val resource: Resource = withTempFile("""
            <!DOCTYPE TS []>
            <TS version="2.1" sourcelanguage="en" language="sv">
            <context>
              <name>testContext1</name>
              <message>
                <source>First source</source>
              </message>
            </context>
            <context>
              <name>testContext2</name>
                <message>
                  <source>Second source</source>
                </message>
            </context>
            </TS>
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(2)
        assertThat(resource.textFlows[0].contents).containsExactly("First source")
        assertThat(getContext(resource.textFlows[0])).isEqualTo("testContext1")
        assertThat(resource.textFlows[1].contents).containsExactly("Second source")
        assertThat(getContext(resource.textFlows[1])).isEqualTo("testContext2")
    }

    @Test
    fun testTSWithComments() {
        val resource: Resource = withTempFile("""
            <!DOCTYPE TS []>
            <TS>
              <context>
                <name>Test</name>
                <message>
                  <extracomment>Extra comment</extracomment>
                  <comment>First comment</comment>
                  <source>Line One</source>
                  <translation>Teststring1</translation>
                </message>
                <message>
                  <comment>Second comment</comment>
                  <source>Line Two</source>
                  <translation>Teststring2</translation>
                </message>
                <message><source>Line Three</source>
                <translation>Teststring3</translation>
                </message>
              </context>
            </TS>
        """.trimIndent(), this::parseTestFile)
        assertThat(resource.textFlows).hasSize(3)
        assertThat(resource.textFlows[0].contents).containsExactly("Line One")
        assertThat(getComment(resource.textFlows[0])).isEqualTo("Extra comment")
        assertThat(resource.textFlows[1].contents).containsExactly("Line Two")
        assertThat(getComment(resource.textFlows[1])).isEqualTo("Second comment")
    }

    @Test
    fun testGeneratedFilename() {
        val document = HDocument().apply {
            docId = "/test/basicts.ts"
            name = "basicts.ts"
            path = "test/"
            contentType = ContentType.PO
            locale = HLocale(org.zanata.common.LocaleId("en"))
            rawDocument = HRawDocument().apply {
                type = DocumentType.TS
            }
        }
        assertThat(adapter.generateTranslationFilename(document, "fr"))
                .isEqualTo("basicts_fr.ts")
        assertThat(adapter.generateTranslationFilename(document, "en_AU"))
                .isEqualTo("basicts_en_AU.ts")
    }

    @Test
    fun testGeneratedFilenameMissingExtension() {
        val document = HDocument().apply {
            docId = "/test/basicts"
            name = "basicts"
            path = "test/"
            contentType = ContentType.PO
            locale = HLocale(org.zanata.common.LocaleId("en"))
            rawDocument = HRawDocument().apply {
                type = DocumentType.TS
            }
        }
        assertThat(adapter.generateTranslationFilename(document, "fr"))
                .isEqualTo("basicts_fr.ts")
        assertThat(adapter.generateTranslationFilename(document, "en_AU"))
                .isEqualTo("basicts_en_AU.ts")
    }

    @Test
    fun testUploadedTranslationsFile() {
        withTempFile("""
            <!DOCTYPE TS []>
            <TS version="2.1" sourcelanguage="en" language="dv-LL">
              <context>
                <name>testContext1</name>
                <message>
                  <source>First source</source>
                  <translation>Foun’dé metalkcta</translation>
                </message>
              </context>
              <context>
              <name>testContext2</name>
                <message>
                  <source>Second source</source>
                  <translation>Tba’dé metalkcta</translation>
                </message>
              </context>
            </TS>
        """.trimIndent()) { file ->
            val transParser = ParserOptions(file.toURI(), FR, "")
            val translationsResource = getAdapter().parseTranslationFile(transParser)

            assertThat(translationsResource.textFlowTargets.size).isEqualTo(2)
            assertThat(translationsResource.textFlowTargets[0].contents)
                    .containsExactly("Foun’dé metalkcta")
            assertThat(translationsResource.textFlowTargets[1].contents)
                    .containsExactly("Tba’dé metalkcta")
        }
    }

    @Test
    fun testTranslatedTSDocument() {
        testTranslatedTSDocument(false)
    }

    @Test
    fun testTranslatedTSDocumentApprovedOnly() {
        testTranslatedTSDocument(true)
    }

    private fun testTranslatedTSDocument(approvedOnly: Boolean) {
        withTempFile("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE TS []>
            <TS version="2.1" language="en_US">
            <context>
              <name>testContext1</name>
              <message>
                <source>First source</source>
              </message>
            </context>
            <context>
              <name>testContext2</name>
              <message>
                <source>Second source</source>
              </message>
            </context>
            <context>
              <name>testContext3</name>
              <message>
                <source>Third source</source>
              </message>
            </context>
            </TS>
        """.trimIndent()) { originalFile ->
            val resource = parseTestFile(originalFile)
            val transResource = TranslationsResource()
            transResource.textFlowTargets.addAll(listOf(
                    TextFlowTarget().apply {
                        resId = resource.textFlows[0].id
                        contents = listOf("Foun’dé metalkcta")
                        state = ContentState.Approved
                    },
                    TextFlowTarget().apply {
                        resId = resource.textFlows[1].id
                        contents = listOf("Tba’dé metalkcta")
                        state = ContentState.Translated
                    },
                    TextFlowTarget().apply {
                        resId = resource.textFlows[2].id
                        contents = listOf("Kba’dé metalkcta")
                        state = ContentState.NeedReview
                    }
            ))
            val outputStream = ByteArrayOutputStream()
            val sourceOptions = ParserOptions(originalFile.toURI(), EN, "")
            val translatedDoc = TranslatedDoc(resource, transResource,
                    org.zanata.common.LocaleId("mo-PH"))
            val options = WriterOptions(sourceOptions, translatedDoc)
            adapter.writeTranslatedFile(outputStream, options, approvedOnly)
            // The second translation (Translated) should only have
            // type=unfinished if approvedOnly is set. The third (NeedReview)
            // should always have type=unfinished
            val maybeTypeUnfinished = if (approvedOnly) " type=\"unfinished\"" else ""
            assertThat(outputStream.toString(Charsets.UTF_8)).isEqualTo("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE TS []>
            <TS version="2.1" language="mo-PH">
            <context>
              <name>testContext1</name>
              <message>
                <source>First source</source>
            <translation variants="no">Foun’dé metalkcta</translation>
              </message>
            </context>
            <context>
              <name>testContext2</name>
              <message>
                <source>Second source</source>
            <translation$maybeTypeUnfinished variants="no">Tba’dé metalkcta</translation>
              </message>
            </context>
            <context>
              <name>testContext3</name>
              <message>
                <source>Third source</source>
            <translation type="unfinished" variants="no">Kba’dé metalkcta</translation>
              </message>
            </context>
            </TS>
            """.trimIndent())
        }
    }

    @Test
    fun testFailToParseTSDocument() {
        try {
            withTempFile("""
                <!DOCTYPE TS []>
                <TS version="2.1" sourcelanguage="en" language="sv">
                  <!-- Contains a double bracket here -->
                  <<context>
                  <name>testContext1</name>
                  <message>
                    <source>First source</source>
                  </message>
                </TS>
            """.trimIndent(), this::parseTestFile)
            fail("Expected a FileFormatAdapterException")
        } catch (ffae: FileFormatAdapterException) {
            assertThat(ffae.message).contains("Unable to parse document")
        }
    }

    @Test
    fun testFailToParseTSTranslation() {
        withTempFile("""
                <!DOCTYPE TS []>
                <TS version="2.1" sourcelanguage="en" language="sv">
                  <!-- Contains a double bracket here -->
                  <<context>
                  <name>testContext1</name>
                  <message>
                    <source>First source</source>
                  </message>
                </TS>
            """.trimIndent()) { transFile ->
            val rawDocument = RawDocument(
                    FileUtils.readFileToString(transFile, Charsets.UTF_8),
                    LocaleId("en"),
                    LocaleId("ru"))
            try {
                getAdapter().parseTranslationFile(rawDocument, "")
                fail("Expected a FileFormatAdapterException")
            } catch (ffae: FileFormatAdapterException) {
                assertThat(ffae.message).contains("Unable to parse translation file")
            }
        }
    }

    @Test
    fun testFailToParseOriginalFile() {
        try {
            TsFilter().use { tsFilter ->
                tsFilter.createFilterWriter().use { filterWriter ->
                    getAdapter().generateTranslatedFile(
                            getTestFile("test-ts-nonexistent.ts").toURI(),
                            HashMap(),
                            LocaleId("en"),
                            filterWriter, "", false)
                }
            }
        } catch (ffae: FileFormatAdapterException) {
            assertThat(ffae.message)
                    .contains("Unable to generate translated document from original")
        }
    }

    @Test
    fun nonDocPartIsCaught() {
        val base = """<TS version="2.1" language="test">"""
        val event = Event().apply {
            eventType = EventType.START_DOCUMENT
            resource = StartDocument().apply {
                skeleton = GenericSkeleton(base)
            }
        }
        try {
            adapter.replaceLocaleInDocPart(event, LocaleId.fromBCP47("en-GB"))
            fail("FileFormatAdapterException expected")
        } catch (ffae: FileFormatAdapterException) {
            // Pass
        }

    }

    @Test
    fun sourceLanguageAttributeIsNotAltered() {
        val base = """<TS version="2.1" language="en-GB" sourcelanguage="mo_PH">"""
        val event = Event().apply {
            eventType = EventType.DOCUMENT_PART
            resource = DocumentPart().apply {
                skeleton = GenericSkeleton(base)
            }
        }
        assertThat(adapter.replaceLocaleInDocPart(event,
                toOkapiLocale(org.zanata.common.LocaleId("en-US"))).toString())
                .describedAs("Source language is not altered")
                .isEqualTo("""<TS version="2.1" language="en-US" sourcelanguage="mo_PH">""")
    }

    @Test
    @Ignore("Currently cannot support locales with user parts")
    fun localeModifiersAreNotLost() {
        val base = """<TS version="2.1" language="en-GB">"""
        val event = Event().apply {
            eventType = EventType.DOCUMENT_PART
            resource = DocumentPart().apply {
                skeleton = GenericSkeleton(base)
            }
        }
        assertThat(adapter.replaceLocaleInDocPart(event,
                toOkapiLocale(org.zanata.common.LocaleId("en-US.UTF8")))
                    .toString())
                .describedAs("Source language is not altered")
                .isEqualTo("""<TS version="2.1" language="en-US.UTF8">""")
    }

    private fun getContext(textFlow: TextFlow): String? {
        val potEntryHeader = textFlow.getExtensions(true)
                .findByType(PotEntryHeader::class.java)
        return potEntryHeader?.context
    }

    private fun getComment(textFlow: TextFlow): String? {
        val simpleComment = textFlow.getExtensions(true)
                .findByType(SimpleComment::class.java)
        return simpleComment?.value
    }

}
