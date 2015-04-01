package org.zanata.action;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.action.validator.NotDuplicateEmail;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.CredentialsDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountActivationKey;
import org.zanata.model.HPerson;
import org.zanata.security.AuthenticationType;
import org.zanata.security.ZanataCredentials;
import org.zanata.security.ZanataOpenId;
import org.zanata.service.EmailService;
import org.zanata.ui.faces.FacesMessages;

@Name("inactiveAccountAction")
@Scope(ScopeType.PAGE)
public class InactiveAccountAction implements Serializable {
    @In
    private AccountDAO accountDAO;

    @In
    private PersonDAO personDAO;

    @In
    private EmailService emailServiceImpl;

    @In
    private ZanataCredentials credentials;

    @In
    private ZanataOpenId zanataOpenId;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private CredentialsDAO credentialsDAO;

    @In
    private AccountActivationKeyDAO accountActivationKeyDAO;

    @Getter
    @Setter
    @Email
    @NotDuplicateEmail(message = "This email address is already taken.")
    private String email;

    private HAccount account;

    private static final long serialVersionUID = 1L;

    private HAccount getAccount() {
        if(account == null) {
            if (credentials.getAuthType() == AuthenticationType.OPENID) {
                // NB: Maybe we can get the authenticated openid from somewhere else
                account =
                    credentialsDAO.findByUser(
                        zanataOpenId.getAuthResult().getAuthenticatedId())
                        .getAccount();
            } else {
                account = accountDAO.getByUsername(credentials.getUsername());
            }
        }
        return account;
    }

    public void sendActivationEmail() {
        HAccount account = getAccount();
        if (account != null) {
            HAccountActivationKey key = accountActivationKeyDAO
                .findByAccountIdAndKeyHash(account.getId(),
                    account.getAccountActivationKey().getKeyHash());
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
                "#{msgs['javax.faces.component.UIInput.REQUIRED']}");
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
}
