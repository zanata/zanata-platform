package org.zanata.action;

import org.apache.commons.lang.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.validator.constraints.NotEmpty;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
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

@Named("passwordResetRequest")
@RequestScoped
@Model
@Transactional
public class PasswordResetRequestAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PasswordResetRequestAction.class);
    private static final long serialVersionUID = 1L;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private UserAccountService userAccountServiceImpl;
    @Inject
    private Messages msgs;
    @Inject
    private AccountActivationKeyDAO accountActivationKeyDAO;
    private String activationKey;
    @NotEmpty
    private String usernameOrEmail;
    private HAccount account;

    @Transactional
    public String requestReset() {
        if (getAccount() == null) {
            return getAccountNoFoundMessage();
        }
        HAccountResetPasswordKey key =
                userAccountServiceImpl.requestPasswordReset(getAccount());
        if (key == null) {
            return getAccountNoFoundMessage();
        }
        if (isAccountWaitingForActivation()) {
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
    // @End /* TODO [CDI] commented out end conversation. verify it still work
    // */

    public String sendActivationEmail(String usernameOrEmail) {
        HAccount account = getAccount(usernameOrEmail);
        if (account != null) {
            HAccountActivationKey key = account.getAccountActivationKey();
            if (key != null) {
                key.setCreationDate(new Date());
                accountActivationKeyDAO.makePersistent(key);
                accountActivationKeyDAO.flush();
                String message = emailServiceImpl.sendActivationEmail(
                        account.getPerson().getName(),
                        account.getPerson().getEmail(),
                        account.getAccountActivationKey().getKeyHash());
                facesMessages.addGlobal(message);
            }
        }
        return "/public/home.xhtml";
    }

    public boolean isAccountWaitingForActivation() {
        HAccount account = getAccount();
        if (account == null) {
            return false;
        }
        return account.getAccountActivationKey() != null;
    }

    public HAccount getAccount() {
        if (account == null) {
            account = getAccount(usernameOrEmail);
        }
        return account;
    }

    private HAccount getAccount(String usernameOrEmail) {
        HAccount account = null;
        if (isEmailAddress(usernameOrEmail)) {
            account = accountDAO.getByEmail(usernameOrEmail);
        }
        // if account still null after try as email
        if (account == null) {
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

    public PasswordResetRequestAction() {
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }

    public String getActivationKey() {
        return this.activationKey;
    }

    public void setUsernameOrEmail(final String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getUsernameOrEmail() {
        return this.usernameOrEmail;
    }
}
