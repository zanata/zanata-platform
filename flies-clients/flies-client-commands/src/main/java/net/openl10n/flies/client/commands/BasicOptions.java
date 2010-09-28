package net.openl10n.flies.client.commands;

public interface BasicOptions
{
   FliesCommand initCommand();

   boolean getDebug();
   boolean getErrors();
   boolean getHelp();
   boolean getQuiet();
   void setDebug(boolean debug);
   void setErrors(boolean errors);
   void setHelp(boolean help);
   void setQuiet(boolean quiet);
   boolean isDebugSet();
   boolean isErrorsSet();
   boolean isQuietSet();

   /**
    * Used to generate the command line interface and its usage help. This name
    * should match the Maven Mojo's 'goal' annotation.
    * 
    * @return
    */
   public String getCommandName();

   /**
    * Used to generate CLI usage help. This description should preferably match
    * the Maven Mojo's Javadoc description.
    * 
    * @return
    */
   public String getCommandDescription();

}
