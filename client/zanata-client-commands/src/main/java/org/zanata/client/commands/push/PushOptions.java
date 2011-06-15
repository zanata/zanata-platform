package org.zanata.client.commands.push;

import java.io.File;

import org.zanata.client.commands.ConfigurableProjectOptions;

public interface PushOptions extends ConfigurableProjectOptions
{
   public String getSourceLang();
   public File getSrcDir();
   public File getTransDir();
   public String getProjectType();
   public boolean getPushTrans();
   public boolean getCopyTrans();
   public String getMergeType();
   public String getSourcePattern();
}

