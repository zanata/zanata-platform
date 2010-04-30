package org.fedorahosted.flies.repository.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	}
	
	@AfterMethod
	public void shutdownEM() {
		em.close();
		em = null;
	}

	protected EntityManager getEm() {
		return em;
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
