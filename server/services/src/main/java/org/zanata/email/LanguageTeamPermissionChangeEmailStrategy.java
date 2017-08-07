package org.zanata.email;

import java.util.List;
import javax.mail.internet.InternetAddress;
import javaslang.collection.Map;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.i18n.Messages;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LanguageTeamPermissionChangeEmailStrategy extends EmailStrategy {
    private final LanguageTeamPermissionChangedEvent changedEvent;
    private final Messages msgs;
    private final String contactCoordinatorLink;

    @Override
    public String getSubject(Messages msgs) {
        return msgs.format("jsf.email.languageteam.permission.Subject",
                changedEvent.getLanguage());
    }

    @Override
    public String getBodyResourceName() {
        return "org/zanata/email/templates/language_team_permission_changed.vm";
    }

    @Override
    public Map<String, Object> makeContext(Map<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        Map<String, Object> context =
                super.makeContext(genericContext, toAddresses);
        List<String> oldPermissions = Lists.newArrayList();
        if (changedEvent.hasNoOldPermissions()) {
            oldPermissions.add(msgs
                    .get("jsf.email.languageteam.permission.old.notInTeam"));
        } else {
            transformPermissionToDescription(changedEvent.getOldPermission(),
                    oldPermissions);
        }
        List<String> newPermissions = Lists.newArrayList();
        if (changedEvent.hasNoNewPermissions()) {
            newPermissions.add(msgs
                    .get("jsf.email.languageteam.permission.new.notInTeam"));
        } else {
            transformPermissionToDescription(changedEvent.getNewPermission(),
                    newPermissions);
        }
        return context.put("language", changedEvent.getLanguage())
                .put("changedByName", changedEvent.getChangedByName())
                .put("oldPermissions", oldPermissions)
                .put("newPermissions", newPermissions)
                .put("contactCoordinatorLink", contactCoordinatorLink)
                .put("toName", toAddresses[0].getPersonal());
    }

    private void transformPermissionToDescription(List<Boolean> permissionList,
            List<String> permissionDescriptions) {
        if (changedEvent.translatorPermissionOf(permissionList)) {
            permissionDescriptions.add(
                    msgs.get("jsf.email.languageteam.permission.isTranslator"));
        }
        if (changedEvent.reviewerPermissionOf(permissionList)) {
            permissionDescriptions.add(
                    msgs.get("jsf.email.languageteam.permission.isReviewer"));
        }
        if (changedEvent.coordinatorPermissionOf(permissionList)) {
            permissionDescriptions.add(msgs
                    .get("jsf.email.languageteam.permission.isCoordinator"));
        }
    }

    @java.beans.ConstructorProperties({ "changedEvent", "msgs",
            "contactCoordinatorLink" })
    public LanguageTeamPermissionChangeEmailStrategy(
            final LanguageTeamPermissionChangedEvent changedEvent,
            final Messages msgs, final String contactCoordinatorLink) {
        this.changedEvent = changedEvent;
        this.msgs = msgs;
        this.contactCoordinatorLink = contactCoordinatorLink;
    }
}
