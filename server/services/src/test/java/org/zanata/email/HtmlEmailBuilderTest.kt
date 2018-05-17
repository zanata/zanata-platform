/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.email

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.jvnet.mock_javamail.Mailbox
import org.zanata.common.ContentState.Approved
import org.zanata.common.ContentState.NeedReview
import org.zanata.common.ContentState.Translated
import org.zanata.i18n.Messages
import org.zanata.i18n.MessagesFactory
import org.zanata.service.tm.merge.TMMergeResult
import org.zanata.service.tm.merge.createTMBands
import org.zanata.service.tm.merge.parseBands
import java.io.File
import java.util.*
import javax.mail.Address
import javax.mail.Message.RecipientType.BCC
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Note: as a side effect, the tests in this class generate HTML/TXT files
 * which can be used to view the generated email contents and so we output
 * the file names on stdout.
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class HtmlEmailBuilderTest {
    companion object {
        private val mailSession = Session.getInstance(Properties())
    }

    @Rule @JvmField
    val name = TestName()

    // generate files in English:
    private val msgs = Messages(Locale.ENGLISH)
    private var fromEmail = "zanata@example.com"
    private var fromName = msgs["jsf.Zanata"]
    private var toName = "User Name[测试]"
    private var toAddress = "username@example.com"
    private var serverURL = "https://zanata.example.com"
    private var toAddr: InternetAddress
    private var toAddresses: Array<InternetAddress>

    private val msgsFactory = object : MessagesFactory() {
        override fun getMessages(locale: Locale): Messages = msgs
    }

    private val emailSender = HtmlEmailSender(serverURL, fromEmail, mailSession, msgsFactory)

    init {
        toAddr = Addresses.getAddress(toAddress, toName)
        toAddresses = arrayOf(toAddr)
    }

    private val Address.emailAddress
            get() = (this as InternetAddress).address
    private val Address.name
        get() = (this as InternetAddress).personal

    private fun checkAddresses(message: MimeMessage) {
        assertThat(message.from.map { it.emailAddress }).containsExactly(fromEmail)
        assertThat(message.from.map { it.name }).containsExactly(fromName)
        assertThat(message.getRecipients(BCC).map { it.emailAddress }).containsExactly(toAddress)
        assertThat(message.getRecipients(BCC).map { it.name }).containsExactly(toName)
    }

    private fun checkGenericFooter(html: String, reason: String?) {
        // a message from the generic email template:
        assertThat(html).contains(msgs["jsf.email.GeneratedFromZanataServerAt"])
        assertThat(html).contains(serverURL)
        if (reason != null) {
            assertThat(html).contains(reason)
        }
    }

    @Before
    fun before() {
        Mailbox.clearAll()
    }

    @After
    fun after() {
        Mailbox.clearAll()
    }

    @Test
    fun `TM Merge Result`() {
        // given
        val mergeResult = createTMMergeResult()
        val context = TMMergeEmailContext(
                listOf(Addresses.getAddress(toAddress, toName)),
                ProjectInfo("Test Project", "$serverURL/project/view/test-project"),
                VersionInfo("master", "$serverURL/iteration/view/test-project/master"),
                IntRange(50, 100))

        // when
        val strategy = TMMergeEmailStrategy(context, mergeResult)
        emailSender.sendMessage(strategy) as MimeMessage

        // then
        val message = Mailbox.get(toAddress).first() as MimeMessage
        val parts = extractMultipart(message)
        writeEmailPartsToFiles(parts)

        checkAddresses(message)
        assertThat(message.subject).isEqualTo(msgs["email.templates.tm_merge.Results"]!!)

        val html = parts.html
        checkGenericFooter(html, msgs["email.templates.tm_merge.TriggeredByYou"]!!)
        assertThat(html).contains(context.project.url)
        assertThat(html).contains(context.version.url)
    }

    private fun writeEmailPartsToFiles(parts: MultipartContents) {
        val outputDir = File("target/test-output")
        val basename = "${javaClass.name}.${name.methodName}".replace(' ', '_')
        val textFile = File(outputDir, "$basename.txt")
        val htmlFile = File(outputDir, "$basename.html")
        outputDir.mkdirs()
        textFile.writeText(parts.text)
        htmlFile.writeText(parts.html)
        println("Email bodies for ${name.methodName} written to:")
        println("  ${textFile.absolutePath}")
        println("  ${htmlFile.absolutePath}")
    }

    private fun createTMMergeResult(): TMMergeResult {
        val tmBandDefs = createTMBands(parseBands("70 80 90"))
        val mergeResult = TMMergeResult(tmBandDefs)
        mergeResult.count(state = Approved, score = 100, chars = 233, words = 98, messages = 12)
        mergeResult.count(state = Translated, score = 100, chars = 505, words = 203, messages = 33)
        mergeResult.count(state = NeedReview, score = 99, chars = 99, words = 20)
        mergeResult.count(state = NeedReview, score = 96, chars = 50, words = 10)
        mergeResult.count(state = NeedReview, score = 86, chars = 998, words = 193, messages = 37)
        mergeResult.count(state = NeedReview, score = 36, chars = 200, words = 43, messages = 16)
        return mergeResult
    }

}

