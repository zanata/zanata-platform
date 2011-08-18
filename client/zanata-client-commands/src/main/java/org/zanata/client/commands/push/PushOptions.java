package org.zanata.client.commands.push;

import java.io.File;
import java.util.List;

import org.zanata.client.commands.ConfigurableProjectOptions;

public interface PushOptions extends ConfigurableProjectOptions
{
   public String getSourceLang();
   public File getSrcDir();
   public File getTransDir();
   public boolean getPushTrans();
   public boolean getCopyTrans();
   public boolean getUseSrcOrder();
   public String getMergeType();

   public List<String> getIncludes();
   public List<String> getExcludes();

   public boolean getDefaultexclude();
}

