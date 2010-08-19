package net.openl10n.flies.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import net.openl10n.flies.FliesDbunitJpaTest;
import net.openl10n.flies.dao.TribeDAO;
import net.openl10n.flies.model.HTribe;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "jpa-tests" })
public class TribeDAOTest extends FliesDbunitJpaTest
{

   private TribeDAO dao;

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml",
              DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/TribesData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      dao = new TribeDAO((Session) getEm().getDelegate());
   }

   @Test
   public void getValidTribeByLocale()
   {
      HTribe tribe = dao.getByLocale("de-DE");
      assertThat(tribe, notNullValue());
      assertThat(tribe.getLocale().getId(), is("de-DE"));
   }

   @Test
   public void getValidTribeById()
   {
      HTribe tribe = dao.findById(1l, false);
      assertThat(tribe, notNullValue());
      assertThat(tribe.getLocale().getId(), is("as-IN"));
   }

}
