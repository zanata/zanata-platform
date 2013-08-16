package org.zanata.webtrans.shared.model;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Hannes Eskebaek
 */
public class TextFlowTarget implements HasIdentifier<TextFlowTargetId>, IsSerializable
{
   private TextFlowTargetId id;
   private Locale locale;
   private String content;
   private String displayName;

   // for GWT
   @SuppressWarnings("unused")
   private TextFlowTarget()
   {
   }

   public TextFlowTarget(TextFlowTargetId id, Locale locale, String content, String displayName)
   {
      Preconditions.checkNotNull(id, "localeId cannot be null");
      this.id = id;
      this.locale = locale;
      this.content = content;
      this.displayName = displayName;
   }

   public TextFlowTargetId getId()
   {
      return id;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public String getContent()
   {
      return content;
   }

   public String getDisplayName()
   {
      return displayName;
   }
}
