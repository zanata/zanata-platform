package org.zanata.action;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccountResetPasswordKey;

@Name("passwordReset")
@Scope(ScopeType.CONVERSATION)
public class PasswordResetAction implements Serializable {

    private static final long serialVersionUID = -3966625589007754411L;

    @In
    private EntityManager entityManager;

    @In
    private IdentityManager identityManager;

    @In
    private Messages msgs;

    @Getter
    private String activationKey;

    @Getter
    @Setter
    @NotEmpty
    @Size(min = 6, max = 20)
    private String password;

    @Getter
    private String passwordConfirm;

    @Getter
    private HAccountResetPasswordKey key;


    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
        validatePasswordsMatch();
    }

    public boolean validatePasswordsMatch() {
        if (password == null || !password.equals(passwordConfirm)) {
            FacesMessages.instance().addToControl("passwordConfirm",
                    "Passwords do not match");
            return false;
        }
        return true;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
        key =
                entityManager.find(HAccountResetPasswordKey.class,
                        getActivationKey());
    }

    @Begin(join = true)
    public void validateActivationKey() {

        if (getActivationKey() == null)
            throw new KeyNotFoundException();

        key =
                entityManager.find(HAccountResetPasswordKey.class,
                        getActivationKey());

        if (key == null)
            throw new KeyNotFoundException();
    }

    private boolean passwordChanged;

    @End
    public String changePassword() {

        if (!validatePasswordsMatch())
            return null;

        new RunAsOperation() {
            public void execute() {
                try {
                    passwordChanged =
                            identityManager.changePassword(getKey()
                                    .getAccount().getUsername(), getPassword());
                } catch (AuthorizationException e) {
                    passwordChanged = false;
                    FacesMessages.instance().add(
                            "Error changing password: " + e.getMessage());
                } catch (NotLoggedInException ex) {
                    passwordChanged = false;
                    FacesMessages.instance().add(
                            "Error changing password: " + ex.getMessage());
                }
            }
        }.addRole("admin").run();

        entityManager.remove(getKey());

        if (passwordChanged) {
            FacesMessages.instance()
                    .add(msgs.get("jsf.password.change.success"));
            return "/account/login.xhtml";
        } else {
            FacesMessages.instance()
                    .add(msgs.get("jsf.password.change.failed"));
            return null;
        }

    }

}
