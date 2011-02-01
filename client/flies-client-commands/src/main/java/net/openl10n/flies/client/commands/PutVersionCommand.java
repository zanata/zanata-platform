package net.openl10n.flies.client.commands;

import java.net.URI;

import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.IProjectIterationResource;
import net.openl10n.flies.rest.dto.ProjectIteration;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutVersionCommand extends ConfigurableCommand
{
   private static final Logger log = LoggerFactory.getLogger(PutVersionCommand.class);

   private final PutVersionOptions opts;

   public PutVersionCommand(PutVersionOptions opts)
   {
      super(opts);
      this.opts = opts;
   }

   @Override
   public void run() throws Exception
   {
      ProjectIteration version = new ProjectIteration();
      version.setId(opts.getVersionSlug());
      version.setName(opts.getVersionName());
      version.setDescription(opts.getVersionDesc());
      log.debug("{}", version);

      IProjectIterationResource iterResource = getRequestFactory().getProjectIteration(opts.getVersionProject(), opts.getVersionSlug());
      URI uri = getRequestFactory().getProjectIterationURI(opts.getVersionProject(), opts.getVersionSlug());
      ClientResponse<?> response = iterResource.put(version);
      ClientUtility.checkResult(response, uri);
   }

}
