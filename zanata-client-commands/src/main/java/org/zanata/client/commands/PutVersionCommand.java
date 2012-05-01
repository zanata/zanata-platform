package org.zanata.client.commands;

import java.net.URI;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.dto.ProjectIteration;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionCommand extends ConfigurableCommand<PutVersionOptions>
{
   private static final Logger log = LoggerFactory.getLogger(PutVersionCommand.class);

   public PutVersionCommand(PutVersionOptions opts)
   {
      super(opts);
   }

   @Override
   public void run() throws Exception
   {
      ProjectIteration version = new ProjectIteration();
      version.setId(getOpts().getVersionSlug());
      log.debug("{}", version);

      IProjectIterationResource iterResource = getRequestFactory().getProjectIteration(getOpts().getVersionProject(), getOpts().getVersionSlug());
      URI uri = getRequestFactory().getProjectIterationURI(getOpts().getVersionProject(), getOpts().getVersionSlug());
      ClientResponse<?> response = iterResource.put(version);
      ClientUtility.checkResult(response, uri);
   }

}
