package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.annotations.CheckRole;
import org.zanata.util.Synchronized;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("userSearch")
// TODO this should probably be ViewScoped or even RequestScoped (with changes in xhtml)
@SessionScoped
@Synchronized
public class UserSearch implements Serializable {
    private static final long serialVersionUID = -4792732235757055958L;
    private List<String> users;

    @Inject
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

    public List<String> getUsers() {
        return users;
    }
}
