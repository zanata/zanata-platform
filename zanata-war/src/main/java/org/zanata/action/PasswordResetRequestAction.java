package org.zanata.action;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.service.EmailService;
import org.zanata.service.UserAccountService;
import org.zanata.ui.faces.FacesMessages;

import java.io.Serializable;
import java.util.Date;

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

    @In
    private Messages msgs;

    @In
    private AccountActivationKeyDAO accountActivationKeyDAO;

    @Setter
    @Getter
    private String activationKey;

    @Setter
    @Getter
    @NotEmpty
    private String usernameOrEmail;

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
            facesMessages.addGlobal(msgs.get("jsf.account.notActivated"));
            return null;
        }

        String message = emailServiceImpl.sendPasswordResetEmail(
            getAccount().getPerson(), key.getKeyHash());
        facesMessages.addGlobal(message);
        return "home";
    }

    private String getAccountNoFoundMessage() {
        facesMessages.addGlobal(msgs.get("jsf.account.notFound"));
        return null;
    }

    @End
    public String sendActivationEmail(String usernameOrEmail) {
        HAccount account = getAccount(usernameOrEmail);
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
                facesMessages.addGlobal(message);
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
            account = getAccount(usernameOrEmail);
        }
        return account;
    }

    private HAccount getAccount(String usernameOrEmail) {
        HAccount account = null;
        if(isEmailAddress(usernameOrEmail)) {
            account = accountDAO.getByEmail(usernameOrEmail);
        }
        //if account still null after try as email
        if (account == null){
            account = accountDAO.getByUsername(usernameOrEmail);
        }
        return account;
    }

    /**
     * Check if input string has '@' sign
     */
    private boolean isEmailAddress(String value) {
        return StringUtils.contains(value, "@");
    }
}
