package net.openl10n.flies.client.commands;


import net.openl10n.flies.rest.client.FliesClientRequestFactory;

public abstract class ProjectCommand implements FliesCommand
{
   private final FliesClientRequestFactory factory;

   public ProjectCommand(FliesClientRequestFactory factory)
   {
      this.factory = factory;
   }

   protected FliesClientRequestFactory getRequestFactory()
   {
      return factory;
   }
}
