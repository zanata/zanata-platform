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
public class PutUserCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutUserCommand.class);
   private final PutUserOptions opts;

   public PutUserCommand(PutUserOptions opts)
   {
      super(opts);
      this.opts = opts;
   }

   public void run() throws Exception
   {
      Account account = new Account();
      account.setEmail(opts.getUserEmail());
      account.setName(opts.getUserName());
      account.setUsername(opts.getUserUsername());
      account.setPasswordHash(opts.getUserPasswordHash());
      account.setApiKey(opts.getUserKey());
      account.setEnabled(!opts.isUserDisabled());
      account.setRoles(new HashSet<String>(Arrays.asList(opts.getUserRoles().split(","))));
      account.setTribes(new HashSet<String>(Arrays.asList(opts.getUserLangs().split(","))));

      log.debug("{}", account);

      IAccountResource iterResource = getRequestFactory().getAccount(opts.getUserUsername());
      URI uri = getRequestFactory().getAccountURI(opts.getUserUsername());
      ClientResponse<?> response = iterResource.put(account);
      ClientUtility.checkResult(response, uri);
   }

}
