package org.zanata.webtrans.shared.model;

import org.zanata.webtrans.shared.model.ValidationAction.State;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds display rules of this validation according to the state
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class ValidationDisplayRules implements IsSerializable
{
   private boolean enabled;
   private boolean locked;

   @SuppressWarnings("unused")
   private ValidationDisplayRules()
   {
   }

   public ValidationDisplayRules(State state)
   {
      updateRules(state);
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

   /**
    * Update validation state 
    * Off     : enabled = false, locked = false;
    * Warning : enabled = true,  locked = false;
    * Error   : enabled = true,  locked = true;
    */
   public void updateRules(State state)
   {
      if (state == State.Off)
      {
         enabled = false;
         locked = false;
      }
      else if (state == State.Warning)
      {
         enabled = true;
         locked = false;
      }
      else if (state == State.Error)
      {
         enabled = true;
         locked = true;
      }
   }
}
