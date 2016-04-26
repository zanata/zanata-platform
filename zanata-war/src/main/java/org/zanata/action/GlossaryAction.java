package org.zanata.action;

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.map.ObjectMapper;
import org.zanata.model.HAccount;
import org.zanata.rest.dto.User;
import org.zanata.rest.editor.dto.Permission;
import org.zanata.rest.editor.service.UserService;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;

import java.io.IOException;
import java.io.Serializable;


@Named("glossaryAction")
@javax.faces.bean.ViewScoped
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
            user = userService.transferToUser(authenticatedAccount, true);
        }
        return user;
    }

    public Permission getUserPermission() {
        Permission permission = new Permission();
        boolean canUpdate = false;
        boolean canInsert = false;
        boolean canDelete = false;

        if(authenticatedAccount != null) {
            canUpdate = identity.hasPermission("", "glossary-update");
            canInsert = identity.hasPermission("", "glossary-insert");
            canDelete = identity.hasPermission("", "glossary-delete");
        }

        permission.put("updateGlossary", canUpdate);
        permission.put("insertGlossary", canInsert);
        permission.put("deleteGlossary", canDelete);

        return permission;
    }

    public String convertToJSON(User user) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(user);
        } catch (IOException e) {
            return this.getClass().getName() + "@"
                + Integer.toHexString(this.hashCode());
        }
    }
}
