package org.zanata.dao;

import javax.persistence.EntityManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * TODO This test is only marked as a RestTest because it is an Arquillian based test. We need to better separate the
 * hierarchy.
 */
@RunWith(Arquillian.class)
public class LocaleMemberDAOITCase
{

   //@In
   private LocaleMemberDAO localeMemberDAO;

   //@In
   private EntityManager em;
   
//   @Override
   protected void prepareDBUnitOperations()
   {
//      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
//      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
//      addBeforeTestOperation(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test(expected = NotLoggedInException.class)
   public void failSaveWhenNotLoggedIn() throws Exception
   {
      HLocale locale = em.find(HLocale.class, new Long(1));
      HAccount account = em.find(HAccount.class, new Long(1));

      assertThat(locale, notNullValue());
      assertThat(account, notNullValue());

      HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true);
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

      HLocale locale = em.find(HLocale.class, new Long(1));
      HAccount account = em.find(HAccount.class, new Long(1));

      assertThat(locale, notNullValue());
      assertThat(account, notNullValue());

      HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true);
      localeMemberDAO.makePersistent(newMember);
   }
}
