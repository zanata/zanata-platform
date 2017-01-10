package org.zanata.client.commands;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.dto.Account;
import com.google.common.base.Splitter;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.Messages.get;


/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserCommand extends ConfigurableCommand<PutUserOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(PutUserCommand.class);

    public PutUserCommand(PutUserOptions opts) {
        super(opts);
    }

    public void run() throws Exception {

        String userName = getOpts().getUserName();
        String userUsername = getOpts().getUserUsername();
        String passwordHash = getOpts().getUserPasswordHash();
        String userEmail = getOpts().getUserEmail();

        Account account = getClientFactory().getAccountClient().get(userUsername);
        boolean newAccount = account == null;

        if (newAccount) {
            log.info("Creating new account {}", userUsername);
            account = new Account();
            if (userEmail == null || userName == null) {
                throw new RuntimeException("New user's name and email must be specified");
            } else if (StringUtils.isBlank(passwordHash)) {
                log.warn("No passwordHash set, user will need to use the Forgot Password feature");
            }
            account.setUsername(userUsername);
        } else {
            log.info("Updating account {}", account.getUsername());
        }

        account.setEmail(firstNonNull(userEmail, account.getEmail()));
        account.setName(firstNonNull(userName, account.getName()));
        account.setApiKey(firstNonNull(getOpts().getUserKey(), account.getApiKey()));
        account.setPasswordHash(firstNonNull(passwordHash, account.getPasswordHash()));
        account.setEnabled(firstNonNull(getOpts().isUserEnabled(), account.isEnabled()));

        Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
        if (getOpts().getUserRoles() != null) {
            account.setRoles(newHashSet(splitter.split(getOpts().getUserRoles())));
        }
        if (getOpts().getUserLangs() != null) {
            account.setTribes(newHashSet(splitter.split(getOpts().getUserLangs())));
        }

        if (getOpts().isInteractiveMode()) {
            log.info("User: {}", account.getUsername());
            log.info("Name: {}", account.getName());
            log.info("Email: {}", userEmail);
            log.info("PasswordHash: {}", firstNonNull(passwordHash, "(unchanged)"));
            log.info("API Key: {}", firstNonNull(getOpts().getUserKey(), "(unchanged)"));
            log.info("Roles: {}", firstNonNull(account.getRoles(), "None"));
            log.info("Languages: {}", firstNonNull(account.getTribes(), "None"));
            log.info("Enabled: {}", account.isEnabled());

            ConsoleInteractor console = new ConsoleInteractorImpl(getOpts());
            console.printf(Question, get("continue.yes.no"));
            console.expectYes();
        }
        log.debug("{}", account);

        getClientFactory().getAccountClient().put(
                account.getUsername(), account);
        log.info("Done.");
    }

}
