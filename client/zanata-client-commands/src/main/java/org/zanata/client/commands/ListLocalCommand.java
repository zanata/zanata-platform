package org.zanata.client.commands;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ZanataProxyFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
// FIXME not implemented yet!
public class ListLocalCommand extends ConfigurableProjectCommand<ConfigurableProjectOptions>
{
   private static final Logger log = LoggerFactory.getLogger(ListLocalCommand.class);

   public ListLocalCommand(ConfigurableProjectOptions opts, ZanataProxyFactory factory)
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
