package org.zanata.action;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.annotations.In;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.ui.faces.FacesMessages;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class AbstractProfileAction {
    protected String name;
    protected String email;
    protected String username;
    protected boolean valid;
    private String activationKey;

    @In
    ZanataIdentity identity;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;

    @In
    PersonDAO personDAO;

    @In
    AccountDAO accountDAO;

    protected void validateEmail(String email) {
        HPerson person = personDAO.findByEmail(email);

        if (person != null && !person.getAccount().equals(authenticatedAccount)) {
            valid = false;
            facesMessages.addToControl("email",
                    "This email address is already taken");
        }
    }

    protected void validateUsername(String username) {
        HAccount account = accountDAO.getByUsername(username);

        if (account != null && !account.equals(authenticatedAccount)) {
            valid = false;
            facesMessages.addToControl("username",
                    "This username is already taken");
        }
    }

    @NotEmpty
    @Size(min = 2, max = 80)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Email
    @NotEmpty
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.validateEmail(email);
        this.email = email;
    }

    @NotEmpty
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z\\d_]{3,20}$",
            message = "{validation.username.constraints}")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        validateUsername(username);
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String keyHash) {
        this.activationKey = keyHash;
    }

    public boolean isValid() {
        return valid;
    }
}
