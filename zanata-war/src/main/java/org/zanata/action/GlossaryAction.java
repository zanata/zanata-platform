package org.zanata.action;

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.model.HAccount;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.dto.User;
import org.zanata.rest.editor.service.UserService;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;

import java.io.Serializable;


@Named("glossaryAction")
@javax.faces.bean.ViewScoped
@ZanataSecured
@CheckLoggedIn
@Slf4j
public class GlossaryAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject @Authenticated
    private HAccount authenticatedAccount;

    @Inject
    private ZanataIdentity identity;

    @Inject
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
