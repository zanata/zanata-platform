package org.zanata.email;

import javaslang.collection.Map;
import org.zanata.i18n.Messages;
import javax.mail.internet.InternetAddress;

import static org.zanata.util.HtmlUtil.textToSafeHtml;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeclineLanguageRequestEmailStrategy extends VelocityEmailStrategy {
    private final String toName;
    private final String roles;
    private final String contactCoordinatorLink;
    private final String localeDisplayName;
    private final String userMessage;

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
        return context.put("toName", toName).put("roles", roles)
                .put("localeDisplayName", localeDisplayName)
                .put("contactCoordinatorLink", contactCoordinatorLink)
                .put("safeHtmlMessage", textToSafeHtml(userMessage));
    }

    @java.beans.ConstructorProperties({ "toName", "roles",
            "contactCoordinatorLink", "localeDisplayName", "userMessage" })
    public DeclineLanguageRequestEmailStrategy(final String toName,
            final String roles, final String contactCoordinatorLink,
            final String localeDisplayName, final String userMessage) {
        this.toName = toName;
        this.roles = roles;
        this.contactCoordinatorLink = contactCoordinatorLink;
        this.localeDisplayName = localeDisplayName;
        this.userMessage = userMessage;
    }
}
