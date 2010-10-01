package net.openl10n.flies.util;

/**
 * ShortStrings are meant for use in logging. They don't incur the cost of
 * shortening until toString() is called. This means they hold on to the entire
 * string, so don't bother keeping them around in memory for long.
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
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
      return shorten(input);
   }

   public static String shorten(String s)
   {
      if (s.length() <= MAX_LENGTH)
         return s;
      return s.substring(0, MAX_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
   }

}
