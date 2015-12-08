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
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.NotLoggedInException;
import org.zanata.ApplicationConfiguration;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.seam.security.AbstractRunAsOperation;
import org.zanata.seam.security.IdentityManager;
import org.zanata.ui.faces.FacesMessages;

@Name("passwordReset")
@Scope(ScopeType.CONVERSATION)
public class PasswordResetAction implements Serializable {

    private static final long serialVersionUID = -3966625589007754411L;

    @In
    private EntityManager entityManager;

    @In
    private IdentityManager identityManager;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private Messages msgs;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @Getter
    private String activationKey;

    @Getter
    @Setter
    @NotEmpty
    @Size(min = 6, max = 1024)
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
            facesMessages.addToControl("passwordConfirm",
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
        if (!applicationConfiguration.isInternalAuth()) {
            throw new AuthorizationException(
                    "Password reset is only available for server using internal authentication");
        }

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

        new AbstractRunAsOperation() {
            public void execute() {
                try {
                    passwordChanged =
                            identityManager.changePassword(getKey()
                                    .getAccount().getUsername(), getPassword());
                } catch (AuthorizationException | NotLoggedInException e) {
                    passwordChanged = false;
                    facesMessages.addGlobal(
                            "Error changing password: " + e.getMessage());
                }
            }
        }.addRole("admin").run();

        entityManager.remove(getKey());

        if (passwordChanged) {
            facesMessages
                    .addGlobal(msgs.get("jsf.password.change.success"));
            return "/account/login.xhtml";
        } else {
            facesMessages
                    .addGlobal(msgs.get("jsf.password.change.failed"));
            return null;
        }

    }

}
