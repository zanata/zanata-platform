package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.annotations.CheckRole;
import org.zanata.security.annotations.ZanataSecured;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("org.jboss.seam.security.management.userSearch")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@ZanataSecured
public class UserSearch implements Serializable {
    private static final long serialVersionUID = -4792732235757055958L;
    @DataModel
    List<String> users;

    @DataModelSelection
    String selectedUser;

    @In
    IdentityManager identityManager;

    @CheckRole("admin")
    public void loadUsers() {
        users = identityManager.listUsers();
    }

    public String getUserRoles(String username) {
        List<String> roles = identityManager.getGrantedRoles(username);

        if (roles == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (String role : roles) {
            sb.append(sb.length() > 0 ? ", " : "").append(role);
        }

        return sb.toString();
    }

    public String getSelectedUser() {
        return selectedUser;
    }
}
