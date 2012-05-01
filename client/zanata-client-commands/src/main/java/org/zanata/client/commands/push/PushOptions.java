package org.zanata.client.commands.push;

import java.util.List;

import org.zanata.client.commands.PushPullOptions;

public interface PushOptions extends PushPullOptions
{
   public String getSourceLang();
   public PushType getPushType();
   public boolean getCopyTrans();
   public String getMergeType();
   public List<String> getIncludes();
   public List<String> getExcludes();
   public boolean getDefaultExcludes();
   public boolean getDeleteObsoleteModules();
}

