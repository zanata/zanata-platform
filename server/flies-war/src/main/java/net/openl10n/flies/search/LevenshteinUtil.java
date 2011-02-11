package net.openl10n.flies.search;

public class LevenshteinUtil
{

   /**
    * Calculates the fuzzy match of needle in haystack, using a modified version
    * of the Levenshtein distance algorithm. The function is modified from the
    * levenshtein function in the bktree module by Adam Hupp. <br/>
    * This version is adapted from the Python version here:
    * http://ginstrom.com/scribbles
    * /2007/12/01/fuzzy-substring-matching-with-levenshtein-distance-in-python/
    */
   public static int getLevenshteinSubstringDistance(String needle, String haystack)
   {
      int m = needle.length();
      int n = haystack.length();

      // base cases
      if (m == 1)
         return haystack.contains(needle) ? 0 : 1;
      if (n == 0)
         return m;

      int[] row1 = new int[n + 1];
      for (int i = 0; i < n + 1; i++)
         row1[i] = 0;
      for (int i = 0; i < m; i++)
      {
         int[] row2 = new int[n + 1];
         row2[0] = i + 1;
         for (int j = 0; j < n; j++)
         {
            int cost = (needle.charAt(i) != haystack.charAt(j)) ? 1 : 0;

            row2[j + 1] = min(row1[j + 1] + 1, // deletion
                  row2[j] + 1, // insertion
                  row1[j] + cost);// substitution
         }
         row1 = row2;
      }
      return min(row1);
   }

   private static int min(int a, int b, int c)
   {
      return Math.min(a, Math.min(b, c));
   }

   private static int min(int[] a)
   {
      int candidate = a[0];
      for (int i = 1; i < a.length; i++)
      {
         if (a[i] < candidate)
            candidate = a[i];
      }
      return candidate;
   }

}
