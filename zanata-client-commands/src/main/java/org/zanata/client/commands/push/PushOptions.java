package org.zanata.client.commands.push;

import java.util.List;

import org.zanata.client.commands.PushPullOptions;

public interface PushOptions extends PushPullOptions
{
   public String getSourceLang();
   public PushPullType getPushType();
   public boolean getCopyTrans();
   public String getMergeType();
   public List<String> getIncludes();
   public List<String> getExcludes();

   public boolean getCaseSensitive();

   public boolean getExcludeLocale();
   public boolean getDefaultExcludes();
   public boolean getDeleteObsoleteModules();
   public int getBatchSize();
}

