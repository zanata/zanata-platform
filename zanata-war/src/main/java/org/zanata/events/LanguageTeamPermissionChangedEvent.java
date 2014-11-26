package org.zanata.events;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Getter
@ToString
public class LanguageTeamPermissionChangedEvent implements Serializable {
    public static final String LANGUAGE_TEAM_PERMISSION_CHANGED =
            "languageTeamPermissionChanged";
    private static final long serialVersionUID = -1L;

    private final LocaleId language;
    private final String name;
    private final String email;
    private final String changedByName;
    private final String changedByEmail;
    private List<Boolean> oldPermission = ImmutableList.of(false, false, false);
    private List<Boolean> newPermission = ImmutableList.of(false, false, false);

    public LanguageTeamPermissionChangedEvent(HPerson person, LocaleId language,
            HPerson doneByPerson) {
        name = person.getName();
        email = person.getEmail();
        this.language = language;
        changedByName = doneByPerson.getName();
        changedByEmail = doneByPerson.getEmail();
    }

    public LanguageTeamPermissionChangedEvent joiningTheTeam(
            boolean isTranslator, boolean isReviewer, boolean isCoordinator) {
        return this.oldPermission(false, false, false)
                .newPermission(isTranslator, isReviewer, isCoordinator);
    }

    public LanguageTeamPermissionChangedEvent updatingPermissions(
            HLocaleMember oldMembership, boolean isTranslator,
            boolean isReviewer, boolean isCoordinator) {
        return this.oldPermission(oldMembership.isTranslator(),
                oldMembership.isReviewer(), oldMembership.isCoordinator())
                .newPermission(isTranslator, isReviewer, isCoordinator);
    }

    public LanguageTeamPermissionChangedEvent changedTranslatorPermission(
            HLocaleMember localeMember) {
        return this.oldPermission(!localeMember.isTranslator(),
                localeMember.isReviewer(),
                localeMember.isCoordinator())
                .newPermission(localeMember.isTranslator(),
                        localeMember.isReviewer(),
                        localeMember.isCoordinator());
    }

    public LanguageTeamPermissionChangedEvent changedReviewerPermission(
            HLocaleMember localeMember) {
        return this.oldPermission(localeMember.isTranslator(),
                !localeMember.isReviewer(),
                localeMember.isCoordinator())
                .newPermission(localeMember.isTranslator(),
                        localeMember.isReviewer(),
                        localeMember.isCoordinator());
    }

    public LanguageTeamPermissionChangedEvent changedCoordinatorPermission(
            HLocaleMember localeMember) {
        return this.oldPermission(localeMember.isTranslator(),
                localeMember.isReviewer(),
                !localeMember.isCoordinator())
                .newPermission(localeMember.isTranslator(),
                        localeMember.isReviewer(),
                        localeMember.isCoordinator());
    }

    private LanguageTeamPermissionChangedEvent oldPermission(
            boolean isTranslator, boolean isReviewer, boolean isCoordinator) {
        oldPermission =
                ImmutableList.of(isTranslator, isReviewer, isCoordinator);
        return this;
    }

    private LanguageTeamPermissionChangedEvent newPermission(
            boolean isTranslator, boolean isReviewer, boolean isCoordinator) {
        newPermission =
                ImmutableList.of(isTranslator, isReviewer, isCoordinator);
        return this;
    }

    public int numOfGrantedOldPermissions() {
        return Iterables.frequency(oldPermission, Boolean.TRUE);
    }

    public int numOfGrantedNewPermissions() {
        return Iterables.frequency(newPermission, Boolean.TRUE);
    }

    public boolean translatorPermissionOf(List<Boolean> permissionList) {
        return getPermission(permissionList, Permission.translator);
    }

    public boolean reviewerPermissionOf(List<Boolean> permissionList) {
        return getPermission(permissionList, Permission.reviewer);
    }

    public boolean coordinatorPermissionOf(List<Boolean> permissionList) {
        return getPermission(permissionList, Permission.coordinator);
    }

    private static Boolean getPermission(List<Boolean> permissionList,
            Permission permission) {
        return permissionList.get(permission.ordinal());
    }

    public boolean isPermissionChanged() {
        return !oldPermission.equals(newPermission);
    }

    private static enum Permission {
        translator, reviewer, coordinator
    }
}
