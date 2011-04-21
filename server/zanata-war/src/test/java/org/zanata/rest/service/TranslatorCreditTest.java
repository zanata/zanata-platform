package org.zanata.rest.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import org.testng.annotations.Test;
import org.zanata.rest.service.TranslatorCredit;

@Test(groups = { "unit-tests" })
public class TranslatorCreditTest
{
   protected TranslatorCredit getAnne2011()
   {
      TranslatorCredit cred = new TranslatorCredit();
      cred.setName("Anne");
      cred.setEmail("anne@example.org");
      cred.setYear(2011);
      return cred;
   }

   protected TranslatorCredit getAnne2012()
   {
      TranslatorCredit cred = new TranslatorCredit();
      cred.setName("Anne");
      cred.setEmail("anne@example.org");
      cred.setYear(2012);
      return cred;
   }
   
   protected TranslatorCredit getBob2010()
   {
      TranslatorCredit cred = new TranslatorCredit();
      cred.setName("Bob");
      cred.setEmail("bob@example.org");
      cred.setYear(2010);
      return cred;
   }
   
   @Test
   public void testToString()
   {
      TranslatorCredit cred = getAnne2011();
      assertThat(cred.toString(), is("Anne <anne@example.org>, 2011."));
   }

   @Test
   public void testSort() throws Exception
   {
      Set<TranslatorCredit> set = new TreeSet<TranslatorCredit>();
      set.add(getAnne2011());
      set.add(getAnne2011());
      set.add(getAnne2012());
      set.add(getBob2010());
      assertThat(set.toString(), is(
            "[Bob <bob@example.org>, 2010., " +
            "Anne <anne@example.org>, 2011., " +
            "Anne <anne@example.org>, 2012.]"));
   }

}
