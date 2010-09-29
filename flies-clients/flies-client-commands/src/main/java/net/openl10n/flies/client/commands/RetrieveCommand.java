package net.openl10n.flies.client.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class RetrieveCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(RetrieveCommand.class);

   public RetrieveCommand(ConfigurableProjectOptions opts)
   {
      super(opts);
   }

   @Override
   public void run()
   {
      // TODO remove this
      log.debug("retrieve");
      // TODO needs DocSet support
   }

}
