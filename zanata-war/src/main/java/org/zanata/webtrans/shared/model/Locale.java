package org.zanata.webtrans.shared.model;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Hannes Eskebaek
 */
public class Locale implements HasIdentifier<IdForLocale>, IsSerializable
{
   private IdForLocale id;
   private String displayName;
   public final static Locale notChosenLocale = new Locale();

   // for GWT
   @SuppressWarnings("unused")
   private Locale()
   {
   }

   public Locale(IdForLocale id, String displayName)
   {
      Preconditions.checkNotNull(id, "localeId cannot be null");
      this.id = id;
      this.displayName = displayName;
   }

   public IdForLocale getId()
   {
      return id;
   }

   public String getDisplayName()
   {
      return displayName;
   }
}
