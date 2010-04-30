package org.fedorahosted.flies;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class FliesJpaTest {

	private static EntityManagerFactory emf;

	protected EntityManager em;
	
	@BeforeMethod
	public void setupEM() {
		em = emf.createEntityManager();
		em.getTransaction().begin();
	}
	
	@AfterMethod
	public void shutdownEM() {
		em.getTransaction().commit();
		em.close();
		em = null;
	}

	protected EntityManager getEm() {
		return em;
	}

	protected Session getSession() {
		return (Session) em.getDelegate();
	}
	
	@BeforeSuite
	public void initializeEMF() {
		emf = Persistence.createEntityManagerFactory("fliesTestDatabase");
	}

	@AfterSuite
	public void shutDownEMF() {
		emf.close();
		emf = null;
	}
	
}
