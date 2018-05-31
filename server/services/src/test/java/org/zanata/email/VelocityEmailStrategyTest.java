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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ProjectType;
import org.zanata.i18n.Messages;
import org.zanata.i18n.MessagesFactory;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import static javax.mail.Message.RecipientType.BCC;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class VelocityEmailStrategyTest {
    // use this if you want to see the real messages on stderr
    private static final boolean DEBUG = false;
    Locale locale = Locale.ENGLISH;

    // context values needed for most/all templates:
    private Messages msgs = DEBUG ? new Messages(locale) : new Messages(locale) {
        private static final long serialVersionUID = 1L;

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
    private String expectedUserMessage = "some &lt;b&gt;HTML&lt;/b&gt;";

    private MessagesFactory msgsFactory = new MessagesFactory() {
        private static final long serialVersionUID = 1L;

        @Override
        public Messages getMessages(Locale locale) {
            return msgs;
        }
    };

    private Session session = Session.getDefaultInstance(new Properties());
    private EmailBuilder.Context context = new EmailBuilder.Context() {
        @Override
        String getFromAddress() {
            return fromAddress;
        }

        @Override
        String getServerPath() {
            return testServerPath;
        }
    };
    private EmailBuilder builder = new EmailBuilder(session, context, msgsFactory);
    private MimeMessage message;

    // context values needed for some templates:
    String key = "123456";
    String passowrdResetKey = "abcdefg";
    String receiver = "Dear receiver";
    String fromLoginName = "LOGIN_NAME[测试]";
    String replyEmail = "REPLY_EMAIL[测试]";
    String userSubject = "USER_SUBJECT[测试]";
    String localeId = "LOCALE_ID";
    String localeNativeName = "LOCALE_NAME[测试]";
    String userMessage = "some <b>HTML</b>";

    public VelocityEmailStrategyTest() throws UnsupportedEncodingException {
        toAddr = Addresses.getAddress(toAddress, toName);
        toAddresses = new InternetAddress[] { toAddr };
    }

    @Before
    public void beforeMethod() {
        message = new MimeMessage(session);
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
        assertThat(html).contains(testServerPath);
    }

    @Test
    public void activation() throws Exception {
        VelocityEmailStrategy strategy =
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

    private String extractHtmlPart(MimeMessage message) {
        return MultipartKt.extractMultipart(message).getHtml();
    }

    @Test
    public void activationAndReset() throws Exception {
        VelocityEmailStrategy strategy =
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
        VelocityEmailStrategy strategy =
                new ContactAdminEmailStrategy(
                        fromLoginName, fromName, replyEmail, userSubject,
                        userMessage);

        builder.buildMessage(message, strategy, toAddresses, Lists.newArrayList("contactAdmin test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.email.admin.SubjectPrefix", fromLoginName) +
                " " + userSubject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
            "jsf.email.admin.UserMessageIntro", fromName, fromLoginName));
        assertThat(html).contains(expectedUserMessage);
    }

    @Test
    public void contactAdminAnonymous() throws Exception {
        String ipAddress = "101.20.30.40";
        VelocityEmailStrategy strategy = new ContactAdminAnonymousEmailStrategy(
                ipAddress, userSubject, userMessage);

        builder.buildMessage(message, strategy, toAddresses,
                Lists.newArrayList("contactAdminAnonymous test"));

        assertThat(message.getSubject()).isEqualTo(msgs.format(
                "jsf.email.admin.SubjectPrefix", ipAddress) +
                " " + userSubject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
                "jsf.email.admin.AnonymousUserMessageIntro", ipAddress));
        assertThat(html).contains(expectedUserMessage);
    }

    @Test
    public void declineLanguageRequest() throws Exception {
        String contactCoordinatorLink = "http://localhost/language";
        String roles = "coordinator, translator";
        String localeDisplayName = "Spanish";

        VelocityEmailStrategy strategy =
            new DeclineLanguageRequestEmailStrategy(
                toName, roles, contactCoordinatorLink, localeDisplayName,
                    userMessage);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("declineLanguageRequest test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
            "jsf.email.languageteam.request.reject.subject", localeDisplayName));

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(msgs.format(
            "jsf.email.languageteam.request.reject.message", roles, localeDisplayName));
        assertThat(html).contains(userMessage);
    }

    @Test
    public void contactLanguageCoordinator() throws Exception {
        VelocityEmailStrategy strategy =
                new ContactLanguageCoordinatorEmailStrategy(receiver,
                        fromLoginName, fromName, replyEmail, userSubject,
                        localeId, localeNativeName, userMessage);

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
        assertThat(html).contains(expectedUserMessage);
        assertThat(html).contains(
                testServerPath + "/language/view/" + localeId);
    }

    @Test
    public void emailValidation() throws Exception {
        VelocityEmailStrategy strategy =
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
        VelocityEmailStrategy strategy =
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
        VelocityEmailStrategy strategy =
                new RequestToJoinLanguageEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        localeId, localeNativeName, userMessage,
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
        assertThat(html).contains(expectedUserMessage);
        assertThat(html).contains(
                testServerPath + "/language/view/" + localeId);
    }

    @Test
    public void contactLanguageMember() throws Exception {
        String subject = "email subject";
        String contactAdminLink = "link";
        VelocityEmailStrategy strategy =
            new ContactLanguageTeamMembersEmailStrategy(
                fromLoginName, subject, localeId, localeNativeName, userMessage,
                contactAdminLink);

        builder.buildMessage(message, strategy, toAddresses,
            Lists.newArrayList("contactLanguageMember test"));

        checkFromAndTo(message);
        assertThat(message.getSubject()).isEqualTo(msgs.format(
            "jsf.email.language.members.SubjectPrefix", localeId, fromLoginName) + " " + subject);

        String html = extractHtmlPart(message);
        checkGenericTemplate(html);

        assertThat(html).contains(userMessage);
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

        VelocityEmailStrategy strategy =
                new RequestToJoinVersionGroupEmailStrategy(
                        fromLoginName, fromName, replyEmail,
                        versionGroupName, versionGroupSlug,
                        projectIterIds, userMessage);

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
        assertThat(html).contains("some &lt;b&gt;HTML&lt;/b&gt;");
        assertThat(html).contains(
                testServerPath + "/version-group/view/" + versionGroupSlug);
    }

    @Test
    public void usernameChanged() throws Exception {
        String newUsername = "NEW_USERNAME[测试]";

        VelocityEmailStrategy strategy =
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
