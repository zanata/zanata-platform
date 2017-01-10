package org.zanata.client.commands;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.dto.Account;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import java.util.Optional;

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

        String userUsername = getOpts().getUserUsername();
        String passwordHash = getOpts().getUserPasswordHash();
        String userEmail = getOpts().getUserEmail();

        Account account = getClientFactory().getAccountClient().get(userUsername);

        if (account == null) {
            log.info("Creating new account {}", userUsername);
            account = new Account();
            if (userEmail == null || userUsername == null) {
                throw new RuntimeException("New user's name and email must be specified");
            } else if (passwordHash == null || StringUtils.isBlank(passwordHash)) {
                log.warn("Unusable passwordHash set, user will need to use the Forgot Password feature");
                account.setPasswordHash("NULL");
            }
            account.setUsername(userUsername);
        } else {
            log.info("Updating account {}", account.getUsername());
        }

        account.setEmail(Optional.ofNullable(userEmail).orElse(account.getEmail()));
        account.setName(Optional.ofNullable(getOpts().getUserName()).orElse(account.getName()));
        account.setApiKey(Optional.ofNullable(getOpts().getUserKey()).orElse(account.getApiKey()));
        account.setPasswordHash(Optional.ofNullable(passwordHash).orElse(account.getPasswordHash()));
        account.setEnabled(!getOpts().isUserDisabled());

        Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
        if (getOpts().getUserRoles() != null) {
            account.setRoles(Sets.newHashSet(splitter.split(getOpts().getUserRoles())));
        }
        if (getOpts().getUserLangs() != null) {
            account.setTribes(
                    Sets.newHashSet(splitter.split(getOpts().getUserLangs())));
        }

        log.debug("{}", account);

        getClientFactory().getAccountClient().put(
                account.getUsername(), account);
    }

}
