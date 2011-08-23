package org.zanata.client.commands.pushGlossary;

import java.io.File;

import org.zanata.client.commands.ConfigurableOptions;

public interface PushGlossaryOptions extends ConfigurableOptions
{
   public File getGlossaryFile();

   public String getSourceLang();

   public String getTransLang();
}