package net.openl10n.flies.dao;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HLocale;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public class LocaleDAOTest extends FliesDbunitJpaTest
{

   private LocaleDAO dao;
   private Log log = Logging.getLog(LocaleDAOTest.class);

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeClass
   void beforeClass()
   {
      Identity.setSecurityEnabled(false);
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new LocaleDAO((Session) getEm().getDelegate());
   }

   @Test
   public void testFindByLocale()
   {
      log.debug("testFindByLocale");
      HLocale hl = dao.findByLocaleId(new LocaleId("de-DE"));
      assertThat(hl.getLocaleId().getId(), is("de-DE"));
   }

}
