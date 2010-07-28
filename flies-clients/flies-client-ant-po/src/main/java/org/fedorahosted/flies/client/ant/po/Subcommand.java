package org.fedorahosted.flies.client.ant.po;


public interface Subcommand
{

   public String getCommandName();

   public String getCommandDescription();

   public boolean getErrors();

   public void setErrors(boolean errors);

   public boolean getHelp();

   public void setHelp(boolean b);

   public void process() throws Exception;
}
