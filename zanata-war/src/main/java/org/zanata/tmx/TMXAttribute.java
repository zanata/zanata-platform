package org.zanata.tmx;

import nu.xom.Element;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;


@SuppressWarnings("null")
enum TMXAttribute
{
   creationdate("creationdate"), changedate("changedate"), srclang("srclang"), tuid("tuid");

   TMXAttribute(String attrName)
   {
      this.attrName = attrName;
   }

   private final String attrName;
   private static ImmutableSet<String> attributeNames;

   public String getAttribute(Element element)
   {
      return element.getAttributeValue(attrName);
   }

   static
   {
      Builder<String> builder = ImmutableSet.builder();
      for (TMXAttribute attr : values())
      {
         builder.add(attr.attrName);
      }
      attributeNames = builder.build();
   }

   public static boolean contains(String attributeName)
   {
      return attributeNames.contains(attributeName);
   }
}

