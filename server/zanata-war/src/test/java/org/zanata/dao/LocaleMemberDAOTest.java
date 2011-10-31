package org.zanata.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.ZanataDBUnitSeamTest;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;

@Test(groups = { "seam-tests" })
public class LocaleMemberDAOTest extends ZanataDBUnitSeamTest
{

   private EntityManager em;
   
   private LocaleMemberDAO localeMemberDAO;
   
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("META-INF/testdata/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @BeforeClass
   public void initializeTest()
   {      
      EntityManagerFactory emf=Persistence.createEntityManagerFactory("zanataTestDatasource");
      this.em = emf.createEntityManager();
   }
   
   public void failSaveWhenNotLoggedIn() throws Exception
   {      
      new ComponentTest()
      {
         
         @Override
         protected void testComponents() throws Exception
         {
            try
            {
               localeMemberDAO = (LocaleMemberDAO)getInstance("localeMemberDAO");
               
               HLocale locale = em.find(HLocale.class, new Long(1));
               HAccount account = em.find(HAccount.class, new Long(1));
               
               Assert.assertNotNull(locale);
               Assert.assertNotNull(account);
               
               HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true);
               localeMemberDAO.makePersistent(newMember);
            }
            catch (Exception e)
            {
               Assert.assertTrue(e instanceof NotLoggedInException);
            }
         }
      }.run();
   }
   
   public void testSave() throws Exception
   {
      new ComponentTest()
      {
         
         @Override
         protected void testComponents() throws Exception
         {
            Identity identity = Identity.instance();
            identity.getCredentials().setUsername("admin");
            identity.getCredentials().setPassword("admin");
            identity.login();
            
            localeMemberDAO = (LocaleMemberDAO)getInstance("localeMemberDAO");
            
            HLocale locale = em.find(HLocale.class, new Long(1));
            HAccount account = em.find(HAccount.class, new Long(1));
            
            Assert.assertNotNull(locale);
            Assert.assertNotNull(account);
            
            HLocaleMember newMember = new HLocaleMember(account.getPerson(), locale, true);
            localeMemberDAO.makePersistent(newMember);
         }
      }.run();
   }
}
