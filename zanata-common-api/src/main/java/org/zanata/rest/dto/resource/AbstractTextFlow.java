package org.zanata.rest.dto.resource;

import org.zanata.common.LocaleId;


public abstract class AbstractTextFlow
{
   public abstract void setId(String id);

   public abstract void setContent(String content);

   @SuppressWarnings("rawtypes")
   public abstract ExtensionSet getExtensionsSimpleComment(boolean createIfNull);
   

   // Optional method
   public void setLang(LocaleId lang)
   {
      // To be overridden by child
   }
   
   // Optional method
   public void setDescription(String description)
   {
      // To be overridden by child
   }

}
