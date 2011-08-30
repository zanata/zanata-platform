package org.zanata.client.commands.pushGlossary;

import java.io.File;
import java.util.List;

import org.zanata.client.commands.ConfigurableProjectOptions;

public interface PushGlossaryOptions extends ConfigurableProjectOptions
{
   public File getGlossaryFile();

   public String getSourceLang();

   public String getTransLang();

   public boolean getAllTransComments();

   public List<String> getCommentHeaders();
}