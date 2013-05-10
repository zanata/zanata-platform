package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class ValidationInfo implements IsSerializable
{
   private boolean enabled;
   private boolean locked;
   
   @SuppressWarnings("unused")
   private ValidationInfo()
   {
   }

   public ValidationInfo(boolean enabled)
   {
      this.enabled = enabled;
   }
   
   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }
   
   public boolean isLocked()
   {
      return locked;
   }
   
   public void setLocked(boolean locked)
   {
      this.locked = locked;
   }
   
}
