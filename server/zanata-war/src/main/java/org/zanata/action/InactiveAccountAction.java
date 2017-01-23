package org.zanata.action;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.exception.AuthorizationException;
import org.zanata.action.validator.NotDuplicateEmail;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.CredentialsDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HPerson;
import org.zanata.model.validator.EmailDomain;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataOpenId;
import org.zanata.service.EmailService;
import org.zanata.ui.faces.FacesMessages;

@Named("inactiveAccountAction")
@ViewScoped
@Model
@Transactional
public class InactiveAccountAction implements Serializable {

    @Inject
    private AccountDAO accountDAO;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private ZanataCredentials credentials;
    @Inject
    private ZanataOpenId zanataOpenId;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private CredentialsDAO credentialsDAO;
    @Inject
    private AccountActivationKeyDAO accountActivationKeyDAO;
    @Inject
    private AuthenticationManager authenticationManager;
    @Email
    @NotDuplicateEmail(message = "This email address is already taken.")
    @EmailDomain
    private String email;
    private HAccount account;
    private static final long serialVersionUID = 1L;

    @PostConstruct
    public void onCreate() {
        if (!authenticationManager
                .isAuthenticatedAccountWaitingForActivation()) {
            throw new AuthorizationException(
                    "Account is not waiting for activation");
        }
    }

    private HAccount getAccount() {
        if (account == null) {
            if (credentials.getAuthType() == AuthenticationType.OPENID) {
                // NB: Maybe we can get the authenticated openid from somewhere
                // else
                account = credentialsDAO.findByUser(
                        zanataOpenId.getAuthResult().getAuthenticatedId())
                        .getAccount();
            } else {
                account = accountDAO.getByUsername(credentials.getUsername());
            }
        }
        return account;
    }

    @Transactional
    public String sendActivationEmail() {
        HAccount account = getAccount();
        if (account != null) {
            HAccountActivationKey key = accountActivationKeyDAO
                    .findByAccountIdAndKeyHash(account.getId(),
                            account.getAccountActivationKey().getKeyHash());
            key.setCreationDate(new Date());
            accountActivationKeyDAO.makePersistent(key);
            accountActivationKeyDAO.flush();
            String message = emailServiceImpl.sendActivationEmail(
                    account.getPerson().getName(),
                    account.getPerson().getEmail(),
                    account.getAccountActivationKey().getKeyHash());
            facesMessages.addGlobal(message);
        }
        return "success";
    }

    @Transactional
    public String changeEmail() {
        if (validateEmail(email)) {
            HPerson person =
                    personDAO.findById(getAccount().getPerson().getId(), true);
            person.setEmail(email);
            personDAO.makePersistent(person);
            personDAO.flush();
            getAccount().getPerson().setEmail(email);
            facesMessages.addGlobal("Email updated.");
            sendActivationEmail();
            return "home";
        }
        return null;
    }

    private boolean validateEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            facesMessages.addToControl("email",
                    "#{msgs[\'javax.faces.component.UIInput.REQUIRED\']}");
            return false;
        }
        HPerson person = personDAO.findByEmail(email);
        if (person != null && !person.getAccount().equals(getAccount())) {
            facesMessages.addToControl("email",
                    "This email address is already taken");
            return false;
        }
        return true;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
