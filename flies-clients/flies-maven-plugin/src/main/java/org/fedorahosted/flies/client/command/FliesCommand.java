package org.fedorahosted.flies.client.command;


public interface FliesCommand
{

   public String getCommandName();

   public String getCommandDescription();

   public boolean getErrors();

   public void setErrors(boolean errors);

   public boolean getHelp();

   public void setHelp(boolean b);

   public void run() throws Exception;
}
