package org.zanata.client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.AccountClient;
import org.zanata.rest.dto.Account;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

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
        Account account = new Account();
        account.setEmail(getOpts().getUserEmail());
        account.setName(getOpts().getUserName());
        account.setUsername(getOpts().getUserUsername());
        account.setPasswordHash(getOpts().getUserPasswordHash());
        account.setApiKey(getOpts().getUserKey());
        account.setEnabled(!getOpts().isUserDisabled());
        Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
        account.setRoles(Sets.newHashSet(splitter.split(getOpts().getUserRoles())));
        account.setTribes(
                Sets.newHashSet(splitter.split(getOpts().getUserLangs())));

        log.debug("{}", account);

        getClientFactory().getAccountClient().put(
                getOpts().getUserUsername(), account);
    }

}
