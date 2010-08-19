package net.openl10n.flies.util;

public class ShortString
{

   private static final int MAX_LENGTH = 66;
   private static final String ELLIPSIS = "…";
   private final String input;

   public ShortString(String input)
   {
      this.input = input;
   }

   @Override
   public String toString()
   {
      if (input.length() <= MAX_LENGTH)
         return input;
      return input.substring(0, MAX_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
   }

}
