package org.zanata.action;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.service.EmailService;
import org.zanata.service.UserAccountService;
import org.zanata.ui.faces.FacesMessages;

@Name("passwordResetRequest")
@NoArgsConstructor
@Scope(ScopeType.EVENT)
@Slf4j
public class PasswordResetRequestAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In("jsfMessages")
    private FacesMessages facesMessages;
    @In
    private AccountDAO accountDAO;
    @In
    private EmailService emailServiceImpl;
    @In
    private UserAccountService userAccountServiceImpl;

    private String username;
    private String email;
    private String activationKey;

    private HAccount account;

    public HAccount getAccount() {
        return account;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotEmpty
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z\\d_]{3,20}$")
    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @org.hibernate.validator.constraints.Email
    @NotEmpty
    public String getEmail() {
        return email;
    }

    @End
    public String requestReset() {
        account = accountDAO.getByUsernameAndEmail(username, email);
        HAccountResetPasswordKey key =
                userAccountServiceImpl.requestPasswordReset(account);

        if (key == null) {
            facesMessages.addGlobal("No such account found");
            return null;
        } else {
            String message =
                    emailServiceImpl.sendPasswordResetEmail(account.getPerson(),
                            key.getKeyHash());
            facesMessages.addGlobal(message);
            return "/home.xhtml";
        }

    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

}
