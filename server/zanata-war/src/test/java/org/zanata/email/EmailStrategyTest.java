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
package org.zanata.email;

import static javax.mail.Message.RecipientType.BCC;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ProjectType;
import org.zanata.i18n.Messages;
import org.zanata.i18n.MessagesFactory;
import org.zanata.webtrans.shared.model.ProjectIterationId;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class EmailStrategyTest {
    // use this if you want to see the real messages on stderr
    private static final boolean DEBUG = false;
    Locale locale = Locale.ENGLISH;

    // context values needed for most/all templates:
    Messages msgs = DEBUG ? new Messages(locale) : new Messages(locale) {
        @Override
        public String get(Object key) {
            return "MSG:key=" + key;
        }

        @Override
        public String formatWithAnyArgs(String key, Object... args) {
            return get(key) + ",args={" + Joiner.on(',').join(args) + "}";
        }
    };
    String fromAddress = "zanata@example.com";
    String fromName = msgs.get("jsf.Zanata");
    String toName = "User Name[测试]";
    String toAddress = "username@example.com";
    String testServerPath = "https://zanata.example.com";
    InternetAddress toAddr;
    InternetAddress[] toAddresses;

    MessagesFactory msgsFactory = new MessagesFactory() {
        @Override
        public Messages getMessages(Locale locale) {
            return msgs;
        }
    };

    Session session = Session.getDefaultInstance(new Properties());
    EmailBuilder.Context context = new EmailBuilder.Context() {
        @Override
        String getFromAddress() {
            return fromAddress;
        }

        @Override
        String getServerPath() {
            return testServerPath;
        }
    };
    EmailBuilder builder = new EmailBuilder(session, context, msgsFactory);
    MimeMessage message;

    // context values needed for some templates:
    String key = "123456";
    String passowrdResetKey = "abcdefg";
    String receiver = "Dear receiver";
    String fromLoginName = "LOGIN_NAME[测试]";
    String replyEmail = "REPLY_EMAIL[测试]";
    String userSubject = "USER_SUBJECT[测试]";
    String localeId = "LOCALE_ID";
    String localeNativeName = "LOCALE_NAME[测试]";
    String htmlMessage = "some <b>HTML</b>";

    public EmailStrategyTest() throws UnsupportedEncodingException {
        toAddr = Addresses.getAddress(toAddress, toName);
        toAddresses = new InternetAddress[] { toAddr };
    }

    @Before
    public void beforeMethod() {
        message = new MimeMessage(session);
    }

    private String extractHtmlPart(MimeMessage message)
            throws IOException, MessagingException {
        if (DEBUG) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            message.writeTo(os);
            System.err.println(os.toString("UTF-8"));
        }

        Multipart multipart = (Multipart) message.getContent();
        // one for html, one for text
        assertThat(multipart.getCount()).isEqualTo(2);

        // Text should appear first (because HTML is the preferred format)
        BodyPart textPart = multipart.getBodyPart(0);
        assertThat(textPart.getDataHandler().getContentType()).isEqualTo(
                "text/plain; charset=UTF-8");

        BodyPart htmlPart = multipart.getBodyPart(1);
        assertThat(htmlPart.getDataHandler().getContentType()).isEqualTo(
                "text/html; charset=UTF-8");
        String htmlContent = (String) htmlPart.getContent();

        return htmlContent;
    }

    private void checkFromAndTo(MimeMessage message) throws MessagingException {
        assertThat(message.getFrom()).extracting("address").contains(
                fromAddress);
        assertThat(message.getFrom()).extracting("personal").contains(
            fromName);
        assertThat(message.getRecipients(BCC)).extracting("address").contains(
            toAddress);
        assertThat(message.getRecipients(BCC)).extracting("personal").contains(
                toName);
    }

    private void checkGenericTemplate(String html) {
        // a message from the generic email template:
        assertThat(html).contains(msgs.get(
                "jsf.email.GeneratedFromZanataServerAt"));
    }

    @Test
    public void activation() throws Exception {
        EmailStrategy strategy =
                new ActivationEmailStrategy(key);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("activation test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.get(
                "jsf.email.activation.Subject"));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.get(
                "jsf.email.activation.ClickLinkToActivateAccount"));
        assertThat(html).contains(
                testServerPath + "/account/activate/123456");
    }

    @Test
    public void activationAndReset() throws Exception {
        EmailStrategy strategy =
                new ActivationEmailStrategy(key, passowrdResetKey);

        builder.buildMessage(message, strategy, toAddresses,
                Lists.newArrayList("activation test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.get(
                "jsf.email.activation.Subject"));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.get(
                "jsf.email.activation.ClickLinkToActivateAccount"));
        assertThat(html).contains(
                testServerPath + "/account/activate/123456?resetPasswordKey=" + passowrdResetKey);
    }

    @Test
    public void contactAdmin() throws Exception {
        EmailStrategy strategy =
                new ContactAdminEmailStrategy(
                        fromLoginName, fromName, replyEmail, userSubject,
                        htmlMessage);

        builder.buildMessage(message, strategy, toAddresses, Lists.newArrayList("contactAdmin test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.email.admin.SubjectPrefix", fromLoginName) +
                " " + userSubject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
            "jsf.email.admin.UserMessageIntro", fromName, fromLoginName));
        assertThat(html).contains(
                htmlMessage);
    }

    @Test
    public void declineLanguageRequest() throws Exception {
        String contactCoordinatorLink = "http://localhost/language";
        String roles = "coordinator, translator";
        String localeDisplayName = "Spanish";

        EmailStrategy strategy =
            new DeclineLanguageRequestEmailStrategy(
                toName, roles, contactCoordinatorLink, localeDisplayName,
                htmlMessage);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("declineLanguageRequest test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
            "jsf.email.languageteam.request.reject.subject", localeDisplayName));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
            "jsf.email.languageteam.request.reject.message", roles, localeDisplayName));
        assertThat(html).contains(htmlMessage);
    }

    @Test
    public void contactLanguageCoordinator() throws Exception {
        EmailStrategy strategy =
                new ContactLanguageCoordinatorEmailStrategy(receiver,
                        fromLoginName, fromName, replyEmail, userSubject,
                        localeId, localeNativeName, htmlMessage);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("contactLanguageCoordinator test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.email.coordinator.SubjectPrefix", localeId, fromLoginName) +
                " " + userSubject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(receiver);
        assertThat(html).contains(msgs.format(
                "jsf.email.coordinator.UserMessageIntro",
                fromName, fromLoginName, localeId, localeNativeName));
        assertThat(html).contains(
                htmlMessage);
        assertThat(html).contains(
                testServerPath + "/language/view/" + localeId);
    }

    @Test
    public void emailValidation() throws Exception {
        EmailStrategy strategy =
                new EmailValidationEmailStrategy(key);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("emailValidation test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.get(
                "jsf.email.accountchange.Subject"));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        // a message from the template:
        assertThat(html).contains(msgs.get(
                "jsf.email.accountchange.ConfirmationLink"));
        assertThat(html).contains(
                testServerPath + "/account/validate_email/123456");
    }

    @Test
    public void passwordReset() throws Exception {
        EmailStrategy strategy =
                new PasswordResetEmailStrategy(key);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("passwordReset test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.get(
                "jsf.email.passwordreset.Subject"));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.get(
                "jsf.email.passwordreset.FollowLinkToResetPassword"));
        assertThat(html).contains(
                testServerPath + "/account/password_reset/123456");
    }

    @Test
    public void requestToJoinLanguage() throws Exception {
        EmailStrategy strategy =
                new RequestToJoinLanguageEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        localeId, localeNativeName, htmlMessage,
                        true, true, true);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("requestToJoinLanguage test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.language.email.joinrequest.Subject", fromLoginName, localeId));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
                "jsf.email.joinrequest.UserRequestingToJoin",
                fromName, fromLoginName, localeId, localeNativeName));
        assertThat(html).contains(htmlMessage);
        assertThat(html).contains(
                testServerPath + "/language/view/" + localeId);
    }

    @Test
    public void contactLanguageMember() throws Exception {
        String subject = "email subject";
        String contactAdminLink = "link";
        EmailStrategy strategy =
            new ContactLanguageTeamMembersEmailStrategy(
                fromLoginName, subject, localeId, localeNativeName, htmlMessage,
                contactAdminLink);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("contactLanguageMember test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
            "jsf.email.language.members.SubjectPrefix", localeId, fromLoginName) + " " + subject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(htmlMessage);
        assertThat(html).contains(contactAdminLink);
    }

    @Test
    public void requestToJoinVersionGroup() throws Exception {
        String versionGroupName = "GROUP_NAME[测试]";
        String versionGroupSlug = "GROUP_SLUG";
        Collection<ProjectIterationId> projectIterIds = Lists.newArrayList(
                new ProjectIterationId("PROJECT_SLUG", "ITERATION_SLUG",
                        ProjectType.File)
        );

        EmailStrategy strategy =
                new RequestToJoinVersionGroupEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        versionGroupName, versionGroupSlug,
                        projectIterIds, htmlMessage);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("requestToJoinVersionGroup test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.email.JoinGroupRequest.Subject", versionGroupName));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
                "jsf.email.joingrouprequest.RequestingToJoinGroup",
                fromName, fromLoginName, versionGroupName));
        assertThat(html).contains(
                htmlMessage);
        assertThat(html).contains(
                testServerPath + "/version-group/view/" + versionGroupSlug);
    }

    @Test
    public void usernameChanged() throws Exception {
        String newUsername = "NEW_USERNAME[测试]";

        EmailStrategy strategy =
                new UsernameChangedEmailStrategy(newUsername, true);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("usernameChanged test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.get(
                "jsf.email.usernamechange.Subject"));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
                "jsf.email.usernamechange.YourNewUsername", newUsername));
        assertThat(html).contains(
                testServerPath + "/account/password_reset_request");
    }

}
