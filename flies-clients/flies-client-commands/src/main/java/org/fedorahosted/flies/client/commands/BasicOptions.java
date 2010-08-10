package org.fedorahosted.flies.client.commands;

public interface BasicOptions
{
   static final BasicOptions EMPTY = new BasicOptions()
   {
      @Override
      public boolean getDebug()
      {
         return false;
      }

      @Override
      public boolean getErrors()
      {
         return false;
      }

      @Override
      public boolean getHelp()
      {
         return false;
      }

      @Override
      public boolean getQuiet()
      {
         return false;
      }
   };

   boolean getDebug();
   boolean getErrors();
   boolean getHelp();
   boolean getQuiet();

}
