package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class ValidationInfo implements IsSerializable
{
   private ValidationId id;
   private String description;
   private boolean enabled;
   
   private ValidationInfo()
   {
   }

   public ValidationInfo(ValidationId id, String description, boolean enabled)
   {
      this.id = id;
      this.description = description;
      this.enabled = enabled;
   }
   
   public ValidationId getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }
}
