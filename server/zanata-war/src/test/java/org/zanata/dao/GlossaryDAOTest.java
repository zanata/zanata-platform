package org.zanata.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.hibernate.Session;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;

@Test(groups = { "jpa-tests" })
public class GlossaryDAOTest extends ZanataDbunitJpaTest
{
   IMocksControl control = EasyMock.createControl();

   <T> T createMock(String name, Class<T> toMock)
   {
      T mock = control.createMock(name, toMock);
      return mock;
   }

   private GlossaryDAO dao;
   private Log log = Logging.getLog(GlossaryDAOTest.class);

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/GlossaryData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new GlossaryDAO((Session) getEm().getDelegate());
   }

   @Test
   public void testGetEntryById()
   {
      log.debug("testGetEntryById");
      HGlossaryEntry entry = dao.getEntryById(new Long(1));

      Assert.assertNotNull(entry);
      assertThat(entry.getGlossaryTerms().size(), is(2));
   }

   @Test
   public void testGetTermEntryAndLocale()
   {
      HGlossaryEntry mockEntry = createMock("mockEntry", HGlossaryEntry.class);
      EasyMock.expect(mockEntry.getId()).andReturn(new Long(1)).anyTimes();

      EasyMock.replay(mockEntry);
      
      log.debug("testGetTermEntryAndLocale");
      HGlossaryTerm term = dao.getTermByEntryAndLocale(mockEntry.getId(), LocaleId.DE);
      Assert.assertNotNull(term);

   }

   @Test
   public void testGetTermByGlossaryEntryId()
   {
      log.debug("testGetTermByGlossaryEntry");
      List<HGlossaryTerm> termList = dao.getTermByGlossaryEntryId(new Long(1));
      assertThat(termList.size(), is(2));

   }

   @Test
   public void testGetTermByLocaleId()
   {
      log.debug("testGetTermByLocaleId");
      List<HGlossaryEntry> entryList = dao.getEntriesByLocaleId(LocaleId.DE);
      assertThat(entryList.size(), is(1));
   }

}
