package org.fedorahosted.flies.client.commands;



public interface FliesCommand extends GlobalOptions
{

   /**
    * Used to generate the command line interface and its usage help. This name
    * should match the Maven Mojo's 'goal' annotation.
    * 
    * @return
    */
   public String getCommandName();

   /**
    * Used to generate CLI usage help. This description preferably match the
    * Maven Mojo's Javadoc description.
    * 
    * @return
    */
   public String getCommandDescription();

   public boolean getDebug();

   public void setDebug(boolean debug);

   public boolean getErrors();

   public void setErrors(boolean errors);

   public boolean getHelp();

   public void setHelp(boolean help);

   public boolean getQuiet();

   public void setQuiet(boolean quiet);

   /**
    * This method is called after all parameters have been set, to allow the
    * Command to initialise objects, load config files, etc.
    * 
    * @throws Exception
    */
   public void initConfig() throws Exception;

   /**
    * Executes the flies Command, using the parameters which have been
    * previously set. This method must be called after initConfig().
    */
   public void run() throws Exception;

}
