package org.zanata.client.commands;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IAccountResource;
import org.zanata.rest.dto.Account;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserCommand extends ConfigurableCommand<PutUserOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PutUserCommand.class);

   public PutUserCommand(PutUserOptions opts)
   {
      super(opts);
   }

   public void run() throws Exception
   {
      Account account = new Account();
      account.setEmail(getOpts().getUserEmail());
      account.setName(getOpts().getUserName());
      account.setUsername(getOpts().getUserUsername());
      account.setPasswordHash(getOpts().getUserPasswordHash());
      account.setApiKey(getOpts().getUserKey());
      account.setEnabled(!getOpts().isUserDisabled());
      account.setRoles(new HashSet<String>(Arrays.asList(getOpts().getUserRoles().split(","))));
      account.setTribes(new HashSet<String>(Arrays.asList(getOpts().getUserLangs().split(","))));

      log.debug("{}", account);

      IAccountResource iterResource = getRequestFactory().getAccount(getOpts().getUserUsername());
      URI uri = getRequestFactory().getAccountURI(getOpts().getUserUsername());
      ClientResponse<?> response = iterResource.put(account);
      ClientUtility.checkResult(response, uri);
   }

}
