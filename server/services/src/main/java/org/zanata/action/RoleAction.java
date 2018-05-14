package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.i18n.Messages;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.ui.faces.FacesMessages;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("zanataRoleAction")
@ViewScoped
@Model
@Transactional
@CheckLoggedIn
public class RoleAction implements Serializable {
    private static final long serialVersionUID = -3830647911484729768L;
    private String originalRoleName;
    private String roleName;
    private List<String> groups;
    private static final int MAX_NAME_SIZE = 255;

    @Inject
    IdentityManager identityManager;

    @Inject
    ZanataIdentity identity;

    @Inject
    FacesMessages facesMessages;

    @Inject
    private Messages msgs;

    public void loadRole() {
        if (isBlank(roleName)) {
            // creating new roleName
            this.originalRoleName = null;
            groups = new ArrayList<>();
        } else {
            this.originalRoleName = roleName;
            groups = identityManager.getRoleGroups(roleName);
        }
    }

    private boolean isNewRole() {
        return originalRoleName == null;
    }

    @SuppressWarnings("unused")
    public boolean validateRoleName(ValueChangeEvent event) {
        String newRoleName = (String) event.getNewValue();
        String componentId = event.getComponent().getId();

        if (newRoleName.length() > MAX_NAME_SIZE) {
            facesMessages.addToControl(componentId,
                    msgs.get("jsf.roles.RoleNameTooLong"));
            return false;
        }
        if (identityManager.roleExists(newRoleName)) {
            facesMessages.addToControl(componentId,
                    msgs.get("jsf.roles.RoleNameUnavailable"));
            return false;
        }
        if (!isNewRole() && !newRoleName.equals(originalRoleName)) {
            facesMessages.addToControl(componentId,
                    msgs.get("jsf.roles.RoleNameUnmodifiable"));
            return false;
        }
        return true;
    }

    public String save() {
        if (isBlank(roleName)) {
            return alertRoleNameFailure(msgs.get("jsf.roles.RoleNameEmpty"));
        } else if (roleName.length() > MAX_NAME_SIZE) {
            return alertRoleNameFailure(msgs.format(
                    "jsf.roles.RoleNameLengthExceeded", MAX_NAME_SIZE));
        }

        if (isNewRole()) {
            if (identityManager.roleExists(roleName)) {
                return alertRoleNameFailure(
                        msgs.get("jsf.roles.RoleNameUnavailable"));
            }
            return saveNewRole();
        } else {
            if (!roleName.equals(originalRoleName)) {
                return alertRoleNameFailure(
                        msgs.get("jsf.roles.RoleNameUnmodifiable"));
            }
            return saveExistingRole();
        }
    }

    private String alertRoleNameFailure(String message) {
        facesMessages.addGlobal(FacesMessage.SEVERITY_ERROR, message);
        setRole(originalRoleName);
        return "failure";
    }

    private String saveNewRole() {
        boolean success = identityManager.createRole(roleName);

        if (success) {
            for (String r : groups) {
                identityManager.addRoleToGroup(roleName, r);
            }

        }

        return "success";
    }

    private String saveExistingRole() {
        List<String> grantedRoles = identityManager.getRoleGroups(roleName);

        if (grantedRoles != null) {
            for (String r : grantedRoles) {
                if (!groups.contains(r)) {
                    identityManager.removeRoleFromGroup(roleName, r);
                }
            }
        }

        for (String r : groups) {
            if (!grantedRoles.contains(r)) {
                identityManager.addRoleToGroup(roleName, r);
            }
        }

        return "success";
    }

    public String getRole() {
        return roleName;
    }

    public List<String> getAssignableRoles() {
        List<String> roles = identityManager.listGrantableRoles();
        roles.remove(roleName);
        return roles;
    }

    public void setRole(String role) {
        this.roleName = role;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
