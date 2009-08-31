package net.openl10n.packaging.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class DbTest {
	protected static EntityManagerFactory emf;

	@BeforeClass
	public static void initializeEMF(){
		emf = Persistence.createEntityManagerFactory("openl10n");
	}
	
	@AfterClass
	public static void shutDownEMF(){
		emf.close();
	}

}
