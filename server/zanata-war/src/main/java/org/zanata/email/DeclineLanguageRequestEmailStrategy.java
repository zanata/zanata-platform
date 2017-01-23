package org.zanata.email;

import javaslang.collection.Map;
import org.zanata.i18n.Messages;
import org.zanata.util.HtmlUtil;
import javax.mail.internet.InternetAddress;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
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
    public Map<String, Object> makeContext(Map<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        Map<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        String safeHTML = HtmlUtil.SANITIZER.sanitize(htmlMessage);
        return context.put("toName", toName).put("roles", roles)
                .put("localeDisplayName", localeDisplayName)
                .put("contactCoordinatorLink", contactCoordinatorLink)
                .put("htmlMessage", safeHTML);
    }

    @java.beans.ConstructorProperties({ "toName", "roles",
            "contactCoordinatorLink", "localeDisplayName", "htmlMessage" })
    public DeclineLanguageRequestEmailStrategy(final String toName,
            final String roles, final String contactCoordinatorLink,
            final String localeDisplayName, final String htmlMessage) {
        this.toName = toName;
        this.roles = roles;
        this.contactCoordinatorLink = contactCoordinatorLink;
        this.localeDisplayName = localeDisplayName;
        this.htmlMessage = htmlMessage;
    }
}
