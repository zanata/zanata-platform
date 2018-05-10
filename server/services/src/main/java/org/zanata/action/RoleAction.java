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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger log = LoggerFactory.getLogger(RoleAction.class);
    private static final long serialVersionUID = -3830647911484729768L;
    private String originalRoleName;
    private String roleName;
    private List<String> groups;
    private final int MAX_NAME_SIZE = 255;

    @Inject
    IdentityManager identityManager;

    @Inject
    ZanataIdentity identity;

    @Inject
    FacesMessages facesMessages;

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
            facesMessages.addToControl(componentId, "Role name too long");
            return false;
        }
        if (identityManager.roleExists(newRoleName)){
            facesMessages.addToControl(componentId, "Role name not available");
            return false;
        }
        if (!isNewRole() && !newRoleName.equals(originalRoleName)) {
            facesMessages.addToControl(componentId,
                    "Role name change not allowed");
            return false;
        }
        return true;
    }

    public String save() {
        if (isBlank(roleName)) {
            facesMessages.addGlobal(
                    FacesMessage.SEVERITY_ERROR, "Empty role name");
            return "failure";
        } else if (roleName.length() > MAX_NAME_SIZE) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_ERROR,
                    "Role name exceeds " + String.valueOf(MAX_NAME_SIZE) +
                            " characters");
            setRole(originalRoleName);
            return "failure";
        }

        if (isNewRole()) {
            if (identityManager.roleExists(roleName)) {
                facesMessages.addGlobal(
                        FacesMessage.SEVERITY_ERROR, "Role name not available");
                setRole(originalRoleName);
                return "failure";
            }
            return saveNewRole();
        } else {
            if (!roleName.equals(originalRoleName)) {
                facesMessages.addGlobal(
                        FacesMessage.SEVERITY_ERROR, "Cannot rename a role");
                setRole(originalRoleName);
                return "failure";
            }
            return saveExistingRole();
        }
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
