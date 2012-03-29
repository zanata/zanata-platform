package org.zanata.search;

import java.util.List;

public class LevenshteinUtil
{

   /**
    * Compute Levenshtein distance.
    * Taken from http://www.merriampark.com/ld.htm (public domain)
    */
   public static int getLevenshteinDistance(String s, String t)
   {
      int d[][]; // matrix
      int n; // length of s
      int m; // length of t
      int i; // iterates through s
      int j; // iterates through t
      char s_i; // ith character of s
      char t_j; // jth character of t
      int cost; // cost

      n = s.length();
      m = t.length();
      if (n == 0)
      {
         return m;
      }
      if (m == 0)
      {
         return n;
      }
      d = new int[n + 1][m + 1];
      for (i = 0; i <= n; i++)
      {
         d[i][0] = i;
      }
      for (j = 0; j <= m; j++)
      {
         d[0][j] = j;
      }

      for (i = 1; i <= n; i++)
      {
         s_i = s.charAt(i - 1);

         for (j = 1; j <= m; j++)
         {
            t_j = t.charAt(j - 1);
            if (s_i == t_j)
            {
               cost = 0;
            }
            else
            {
               cost = 1;
            }
            d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
         }
      }
      return d[n][m];
   }

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

   public static double getSimilarity(final String s1, final String s2)
   {
      int levDistance = getLevenshteinDistance(s1, s2);
      int maxDistance = Math.max(s1.length(), s2.length());
      double similarity = (maxDistance - levDistance) / (double) maxDistance;
      return similarity;
   }

   private static int countExtraStringLengths(List<String> strings, int fromIndex)
   {
      int total = 0;
      for (int i = fromIndex; i < strings.size(); i++)
      {
         String s = strings.get(i);
         total += s.length();
      }
      return total;
   }

   public static double getSimilarity(final List<String> strings1, final List<String> strings2)
   {
      // length of the shorter list
      int minListSize;

      // count the extra strings first:
      int extraStringLengths; // total of "extra" strings in the longer list
      if (strings1.size() < strings2.size())
      {
         minListSize = strings1.size();
         extraStringLengths = countExtraStringLengths(strings2, minListSize);
      }
      else
      {
         minListSize = strings2.size();
         extraStringLengths = countExtraStringLengths(strings1, minListSize);
      }

      // total of Levenshtein distance between corresponding strings in the
      // two lists, plus the length of any extra strings if one list is longer
      int totalLevDistance = extraStringLengths;
      // total of max editing distance between all the corresponding strings,
      // plus length of extra strings
      int totalMaxDistance = extraStringLengths;

      // now count the strings which correspond between both lists
      for (int i = 0; i < minListSize; i++)
      {
         String s1 = strings1.get(i);
         String s2 = strings2.get(i);
         int levenshteinDistance = getLevenshteinDistance(s1, s2);
         totalLevDistance += levenshteinDistance;
         totalMaxDistance += Math.max(s1.length(), s2.length());
      }
      double similarity = (totalMaxDistance - totalLevDistance) / (double) totalMaxDistance;
      return similarity;
   }

   public static double getSubstringSimilarity(final String needle, final String haystack)
   {
      int levDistance = getLevenshteinSubstringDistance(needle, haystack);
      int maxDistance = Math.max(needle.length(), haystack.length());
      double similarity = (maxDistance - levDistance) / (double) maxDistance;
      return similarity;
   }

}
