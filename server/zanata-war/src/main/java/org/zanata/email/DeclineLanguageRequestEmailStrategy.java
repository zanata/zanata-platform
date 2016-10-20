package org.zanata.email;

import com.googlecode.totallylazy.collections.PersistentMap;
import lombok.RequiredArgsConstructor;
import org.zanata.i18n.Messages;
import org.zanata.util.HtmlUtil;

import javax.mail.internet.InternetAddress;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@RequiredArgsConstructor
public class DeclineLanguageRequestEmailStrategy extends EmailStrategy {
    private final String toName;
    private final String roles;
    private final String contactCoordinatorLink;
    private final String localeDisplayName;
    private final String htmlMessage;

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.languageteam.request.reject.subject",
            localeDisplayName);
    }

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/decline_language_team_request.vm";
    }

    @Override
    public PersistentMap<String, Object> makeContext(
        PersistentMap<String, Object> genericContext,
        InternetAddress[] toAddresses) {
        PersistentMap<String, Object> context = super.makeContext(
            genericContext, toAddresses);
        String safeHTML = HtmlUtil.SANITIZER.sanitize(htmlMessage);
        return context
            .insert("toName", toName)
            .insert("roles", roles)
            .insert("localeDisplayName", localeDisplayName)
            .insert("contactCoordinatorLink", contactCoordinatorLink)
            .insert("htmlMessage", safeHTML);
    }
}
