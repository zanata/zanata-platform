package org.zanata.client.commands;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Account;
import com.google.common.base.Splitter;

import java.util.Arrays;

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
    private ConsoleInteractor console;

    public PutUserCommand(PutUserOptions opts) {
        super(opts);
    }

    public PutUserCommand(PutUserOptions opts, RestClientFactory clientFactory) {
        super(opts, clientFactory);
    }

    public PutUserCommand(PutUserOptions opts, RestClientFactory clientFactory,
                          ConsoleInteractor consoleInteractor) {
        super(opts, clientFactory);
        console = consoleInteractor;
    }

    public void run() throws Exception {

        String userName = getOpts().getUserName();
        String userUsername = getOpts().getUserUsername();
        String passwordHash = getOpts().getUserPasswordHash();
        String userEmail = getOpts().getUserEmail();
        String isEnabled = getOpts().isUserEnabled();
        final String[] enabledOptions = {"auto", "true", "false"};

        if (!Arrays.asList(enabledOptions).contains(isEnabled.toLowerCase())) {
            throw new RuntimeException("userEnabled requires true, false or auto");
        }

        Account account = getClientFactory().getAccountClient().get(userUsername);
        boolean newAccount = account == null;
        if (newAccount) {
            log.info("Creating new account {}", userUsername);
            account = new Account();
            if (userEmail == null || userName == null) {
                throw new RuntimeException(get("email.name.required"));
            } else if (StringUtils.isBlank(passwordHash)) {
                log.warn(get("no.passwordhash.set"));
            }
            account.setUsername(userUsername);

            account.setEnabled(isEnabled.equals("auto") || isEnabled.equals("true"));
        } else {
            log.info("Updating account {}", account.getUsername());
            if (!isEnabled.equals("auto")) {
                account.setEnabled(Boolean.parseBoolean(isEnabled));
            }
        }

        account.setEmail(firstNonNull(userEmail, account.getEmail()));
        account.setName(firstNonNull(userName, account.getName()));
        account.setApiKey(firstNonNull(getOpts().getUserKey(), account.getApiKey()));
        account.setPasswordHash(firstNonNull(passwordHash, account.getPasswordHash()));

        Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
        if (getOpts().getUserRoles() != null) {
            account.setRoles(newHashSet(splitter.split(getOpts().getUserRoles())));
        }
        if (getOpts().getUserLangs() != null) {
            account.setTribes(newHashSet(splitter.split(getOpts().getUserLangs())));
        }

        if (getOpts().isInteractiveMode()) {
            if (console == null) console = new ConsoleInteractorImpl(getOpts());
            log.info("User: {}", account.getUsername());
            log.info("Name: {}", account.getName());
            log.info("Email: {}", account.getEmail());
            log.info("PasswordHash: {}", firstNonNull(passwordHash, "(unchanged)"));
            log.info("API Key: {}", firstNonNull(getOpts().getUserKey(), "(unchanged)"));
            log.info("Roles: {}", account.getRoles());
            log.info("Languages: {}", account.getTribes());
            log.info("Enabled: {}", account.isEnabled());

            console.printf(Question, get("continue.yes.no"));
            console.expectYes();
        }
        log.debug("{}", account);

        getClientFactory().getAccountClient().put(
                account.getUsername(), account);
        log.info("Done.");
    }
}
