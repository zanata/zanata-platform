package net.openl10n.flies.model.po;

import java.util.ArrayList;
import java.util.Arrays;

import org.fedorahosted.openprops.Properties;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test(groups = { "unit-tests" })
public class PoUtilityTest
{
   String lineSep = System.getProperty("line.separator");

   @Test
   public void testHeaderEntriesToString() throws Exception
   {
      Properties entries = new Properties();
      assertEquals("", PoUtility.propertiesToHeader(entries));
      entries.setProperty("key", "value");
      assertEquals("key=value" + lineSep, PoUtility.propertiesToHeader(entries));
      entries.setProperty("key2", "value2");
      assertEquals("key=value" + lineSep + "key2=value2" + lineSep, PoUtility.propertiesToHeader(entries));
      entries.keySet().clear();
      entries.setProperty("key with\na newline and spaces", "value");
      assertEquals("key\\ with\\na\\ newline\\ and\\ spaces=value" + lineSep, PoUtility.propertiesToHeader(entries));
   }

   @Test
   public void testStringToHeaderEntries() throws Exception
   {
      Properties expected = new Properties();
      assertEqual(expected, PoUtility.headerToProperties(""));
      expected.setProperty("key", "value");
      assertEqual(expected, PoUtility.headerToProperties("key=value"));
      expected.setProperty("key2", "value2");
      assertEqual(expected, PoUtility.headerToProperties("key=value\nkey2=value2"));
      expected.setProperty("key1", "value1");
      assertEqual(expected, PoUtility.headerToProperties("key=value\nkey2=value2\nkey1=value1"));
   }

   private static void assertEqual(Properties expected, Properties actual)
   {
      assertEquals(expected, actual); // NB: ignores order
      assertEquals(expected.toString(), actual.toString());
   }

   @Test
   public void testConcatFlags()
   {
      assertEquals("a,b", PoUtility.concatFlags(Arrays.asList("a", "b")));
      assertEquals("a", PoUtility.concatFlags(Arrays.asList("a")));
      assertEquals("", PoUtility.concatFlags(new ArrayList<String>()));
   }

   @Test
   public void testConcatRefs()
   {
      assertEquals("a b", PoUtility.concatRefs(Arrays.asList("a", "b")));
      assertEquals("a", PoUtility.concatRefs(Arrays.asList("a")));
      assertEquals("", PoUtility.concatRefs(new ArrayList<String>()));
   }

   @Test
   public void testSplitFlags()
   {
      assertEquals(Arrays.asList("a", "b"), PoUtility.splitFlags("a,b"));
      assertEquals(Arrays.asList("a"), PoUtility.splitFlags("a"));
      assertEquals(new ArrayList<String>(), PoUtility.splitFlags(""));
   }

   @Test
   public void testSplitRefs()
   {
      assertEquals(Arrays.asList("a", "b"), PoUtility.splitRefs("a b"));
      assertEquals(Arrays.asList("a"), PoUtility.splitRefs("a"));
      assertEquals(new ArrayList<String>(), PoUtility.splitRefs(""));
   }

}
