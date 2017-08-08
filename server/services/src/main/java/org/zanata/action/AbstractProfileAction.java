package org.zanata.action;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import javax.inject.Inject;

import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.ui.faces.FacesMessages;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class AbstractProfileAction implements HasUserDetail {


    protected String name;
    protected String email;
    protected String username;
    protected boolean valid;
    private String activationKey;

    @Inject
    ZanataIdentity identity;

    @Inject
    private FacesMessages facesMessages;

    @Inject
    @Authenticated
    HAccount authenticatedAccount;

    @Inject
    PersonDAO personDAO;

    @Inject
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
        if (isUsernameTaken(username)) {
            valid = false;
            facesMessages.addToControl("username",
                    "This username is already taken");
        }
    }

    protected boolean isUsernameTaken(String username) {
        HAccount account = accountDAO.getByUsername(username);
        return account != null && !account.equals(authenticatedAccount);
    }

    @NotEmpty
    @Size(min = 2, max = 80)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.validateEmail(email);
        this.email = email;
    }

    @Override
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

    public int getUsernameMaxLength() {
        return USERNAME_MAX_LENGTH;
    }
}
