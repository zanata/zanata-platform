package org.zanata.client.commands.push;

import java.util.List;

import org.zanata.client.commands.PushPullOptions;

public interface PushOptions extends PushPullOptions
{
   public String getSourceLang();
   public boolean getPushTrans();
   public boolean getCopyTrans();
   public boolean getUseSrcOrder();
   public String getMergeType();

   public List<String> getIncludes();
   public List<String> getExcludes();
   public boolean getDefaultExcludes();
}

