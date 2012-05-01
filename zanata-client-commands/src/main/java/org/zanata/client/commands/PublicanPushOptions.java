package org.zanata.client.commands;

import java.io.File;

/**
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @deprecated
 * @see org.zanata.client.commands.push.PushOptions
 */
public interface PublicanPushOptions extends ConfigurableProjectOptions
{
   public File getSrcDir();
   public File getSrcDirPot();
   public String getSourceLang();
   public boolean getImportPo();
   public boolean getCopyTrans();
   public boolean getValidate();
   public String getMergeType();
}