package net.openl10n.flies.client.commands;

import net.openl10n.flies.rest.client.FliesClientRequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
// FIXME not implemented yet!
public class ListLocalCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(ListLocalCommand.class);

   public ListLocalCommand(ConfigurableProjectOptions opts, FliesClientRequestFactory factory)
   {
      super(opts, factory);
   }

   public ListLocalCommand(ConfigurableProjectOptions opts)
   {
      this(opts, null);
   }

   @Override
   public void run()
   {
      // TODO remove this
      log.debug("listlocal");

      // TODO needs DocSet support
   }

}
