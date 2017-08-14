package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.Synchronized;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("zanataRoleSearch")
// TODO this should probably be ViewScoped or even RequestScoped (plus xhtml changes)
@SessionScoped
@Model
@Transactional
@Synchronized
public class RoleSearch implements Serializable {
    private static final long serialVersionUID = 1734703030195353735L;
    private List<String> roles;

    @Inject
    IdentityManager identityManager;

    @Inject
    private ZanataIdentity identity;

    @PostConstruct
    public void onCreate() {
        identity.checkPermission("seam.role", "read");
    }

    public void loadRoles() {
        roles = identityManager.listRoles();
    }

    public String getRoleGroups(String role) {
        List<String> roles = identityManager.getRoleGroups(role);

        StringBuilder sb = new StringBuilder();

        for (String r : roles) {
            sb.append((sb.length() > 0 ? ", " : "") + r);
        }

        return sb.toString();
    }

    public List<String> getRoles() {
        return roles;
    }

}
