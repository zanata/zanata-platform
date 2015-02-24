package org.zanata.action;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.service.EmailService;
import org.zanata.service.UserAccountService;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Name("passwordResetRequest")
@NoArgsConstructor
@Scope(ScopeType.EVENT)
@Slf4j
public class PasswordResetRequestAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private AccountDAO accountDAO;

    @In
    private EmailService emailServiceImpl;

    @In
    private UserAccountService userAccountServiceImpl;

    @In
    private Messages msgs;

    @In
    private AccountActivationKeyDAO accountActivationKeyDAO;

    @Setter
    @Getter
    @NotEmpty
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z\\d_]{3,20}$",
        message = "{validation.username.constraints}")
    private String username;

    @Setter
    @Getter
    @NotEmpty
    @Email
    private String email;

    @Setter
    @Getter
    private String activationKey;

    private HAccount account;


    public String requestReset() {
        if(getAccount() == null) {
            return getAccountNoFoundMessage();
        }

        HAccountResetPasswordKey key =
            userAccountServiceImpl.requestPasswordReset(getAccount());

        if(key == null) {
            return getAccountNoFoundMessage();
        }

        if(isAccountWaitingForActivation()) {
            FacesMessages.instance().add(msgs.get("jsf.account.notActivated"));
            return null;
        }

        String message = emailServiceImpl.sendPasswordResetEmail(
            getAccount().getPerson(), key.getKeyHash());
        FacesMessages.instance().add(message);
        return "home";
    }

    private String getAccountNoFoundMessage() {
        FacesMessages.instance().add(msgs.get("jsf.account.notFound"));
        return null;
    }

    @End
    public String sendActivationEmail(String username, String email) {
        HAccount account = accountDAO.getByUsernameAndEmail(username, email);
        if(account != null) {
            HAccountActivationKey key = account.getAccountActivationKey();
            if(key != null) {
                key.setCreationDate(new Date());

                accountActivationKeyDAO.makePersistent(key);
                accountActivationKeyDAO.flush();

                String message =
                    emailServiceImpl.sendActivationEmail(
                        account.getPerson().getName(),
                        account.getPerson().getEmail(),
                        account.getAccountActivationKey().getKeyHash());
                FacesMessages.instance().add(message);
            }
        }
        return "/home.xhtml";
    }

    public boolean isAccountWaitingForActivation() {
        HAccount account = getAccount();
        if (account == null) {
            return false;
        }
        return account.getAccountActivationKey() != null;
    }

    public HAccount getAccount() {
        if(account == null) {
            account = accountDAO.getByUsernameAndEmail(username, email);
        }
        return account;
    }
}
