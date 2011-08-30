package org.zanata.client.commands.glossary.push;

import java.io.File;
import java.util.List;

import org.zanata.client.commands.ConfigurableProjectOptions;

public interface GlossaryPushOptions extends ConfigurableProjectOptions
{
   public File getGlossaryFile();

   public String getSourceLang();

   public String getTransLang();

   public boolean getAllTransComments();

   public List<String> getCommentsHeader();
}