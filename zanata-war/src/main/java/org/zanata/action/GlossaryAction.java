package org.zanata.action;

import lombok.extern.slf4j.Slf4j;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HAccount;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.dto.User;
import org.zanata.rest.editor.service.UserService;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;

import java.io.Serializable;


@Name("glossaryAction")
@Scope(ScopeType.PAGE)
@ZanataSecured
@CheckLoggedIn
@Slf4j
public class GlossaryAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private ZanataIdentity identity;

    @In(value = "editor.userService", create = true)
    private UserService userService;

    private User user;

    public User getUser() {
        if (user == null) {
            user = userService.transferToUser(authenticatedAccount);
        }
        return user;
    }

    public Permission getUserPermission() {
        Permission permission = new Permission();
        boolean canUpdate = false;
        boolean canInsert = false;

        if(authenticatedAccount != null) {
            canUpdate = identity.hasPermission("", "glossary-update");
            canInsert = identity.hasPermission("", "glossary-insert");
        }

        permission.put("updateGlossary", canUpdate);
        permission.put("insertGlossary", canInsert);

        return permission;
    }
}
