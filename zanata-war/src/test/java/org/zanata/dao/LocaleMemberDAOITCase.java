package org.zanata.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.annotations.In;
import org.jboss.seam.security.Identity;
import org.junit.Test;
import org.zanata.ArquillianTest;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.provider.DBUnitProvider.DataSetOperation;

public class LocaleMemberDAOITCase extends ArquillianTest
{

   @In
   private LocaleMemberDAO localeMemberDAO;

   @In
   private EntityManager entityManager;
   
   @Override
   protected void prepareDBUnitOperations()
   {
      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   /*
    *  This test method is no longer relevant.
    *  HLocaleMember entities are no longer restricted to authenticated users.
    */
   //@Test(expected = NotLoggedInException.class)
   public void failSaveWhenNotLoggedIn() throws Exception
   {
      HLocale locale = entityManager.find(HLocale.class, new Long(1));
      HAccount account = entityManager.find(HAccount.class, new Long(1));

      assertThat(locale, notNullValue());
      assertThat(account, notNullValue());

      HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true, true, true);
      // Should fail as there is no user logged in
      localeMemberDAO.makePersistent(newMember);
   }

   @Test
   public void testSave() throws Exception
   {
      Identity identity = Identity.instance();
      identity.getCredentials().setUsername("admin");
      identity.getCredentials().setPassword("admin");
      identity.login();

      HLocale locale = entityManager.find(HLocale.class, new Long(1));
      HAccount account = entityManager.find(HAccount.class, new Long(1));

      assertThat(locale, notNullValue());
      assertThat(account, notNullValue());

      HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true, true, true);
      localeMemberDAO.makePersistent(newMember);
   }
}
