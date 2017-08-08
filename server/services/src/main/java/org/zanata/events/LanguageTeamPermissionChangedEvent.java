package org.zanata.events;

import java.io.Serializable;
import java.util.List;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LanguageTeamPermissionChangedEvent implements Serializable {
    private static final long serialVersionUID = -1L;
    private final LocaleId language;
    private final String name;
    private final String email;
    private final String changedByName;
    private List<Boolean> oldPermission = ImmutableList.of(false, false, false);
    private List<Boolean> newPermission = ImmutableList.of(false, false, false);

    public LanguageTeamPermissionChangedEvent(HPerson person, LocaleId language,
            HPerson doneByPerson) {
        name = person.getName();
        email = person.getEmail();
        this.language = language;
        changedByName = doneByPerson.getName();
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

    public LanguageTeamPermissionChangedEvent
            changedTranslatorPermission(HLocaleMember localeMember) {
        return this
                .oldPermission(!localeMember.isTranslator(),
                        localeMember.isReviewer(), localeMember.isCoordinator())
                .newPermission(localeMember.isTranslator(),
                        localeMember.isReviewer(),
                        localeMember.isCoordinator());
    }

    public LanguageTeamPermissionChangedEvent
            changedReviewerPermission(HLocaleMember localeMember) {
        return this.oldPermission(localeMember.isTranslator(),
                !localeMember.isReviewer(), localeMember.isCoordinator())
                .newPermission(localeMember.isTranslator(),
                        localeMember.isReviewer(),
                        localeMember.isCoordinator());
    }

    public LanguageTeamPermissionChangedEvent
            changedCoordinatorPermission(HLocaleMember localeMember) {
        return this.oldPermission(localeMember.isTranslator(),
                localeMember.isReviewer(), !localeMember.isCoordinator())
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
    // when user has no roles assigned or was not part of the team

    public boolean hasNoOldPermissions() {
        return Iterables.frequency(oldPermission, Boolean.TRUE) == 0;
    }
    // when user has no roles assigned or is removed from the team

    public boolean hasNoNewPermissions() {
        return Iterables.frequency(newPermission, Boolean.TRUE) == 0;
    }

    public boolean hasPermissionsChanged() {
        return !oldPermission.equals(newPermission);
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

    private static enum Permission {
        translator,
        reviewer,
        coordinator;

    }

    public LocaleId getLanguage() {
        return this.language;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getChangedByName() {
        return this.changedByName;
    }

    public List<Boolean> getOldPermission() {
        return this.oldPermission;
    }

    public List<Boolean> getNewPermission() {
        return this.newPermission;
    }

    @Override
    public String toString() {
        return "LanguageTeamPermissionChangedEvent(language="
                + this.getLanguage() + ", name=" + this.getName() + ", email="
                + this.getEmail() + ", changedByName=" + this.getChangedByName()
                + ", oldPermission=" + this.getOldPermission()
                + ", newPermission=" + this.getNewPermission() + ")";
    }
}
