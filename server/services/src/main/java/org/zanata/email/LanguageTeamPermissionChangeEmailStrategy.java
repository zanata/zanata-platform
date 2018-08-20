package org.zanata.email;

import java.util.List;
import javax.mail.internet.InternetAddress;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.i18n.Messages;
import com.google.common.collect.Lists;
import cyclops.collections.immutable.PersistentMapX;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LanguageTeamPermissionChangeEmailStrategy extends
        VelocityEmailStrategy {
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
    public PersistentMapX<String, Object> makeContext(PersistentMapX<String, Object> genericContext,
            InternetAddress[] toAddresses) {
        PersistentMapX<String, Object> context =
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
        return context.plus("language", changedEvent.getLanguage())
                .plus("changedByName", changedEvent.getChangedByName())
                .plus("oldPermissions", oldPermissions)
                .plus("newPermissions", newPermissions)
                .plus("contactCoordinatorLink", contactCoordinatorLink)
                .plus("toName", toAddresses[0].getPersonal());
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
