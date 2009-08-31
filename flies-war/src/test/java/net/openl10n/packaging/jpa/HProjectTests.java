package net.openl10n.packaging.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.openl10n.packaging.jpa.document.HDocument;
import net.openl10n.packaging.jpa.document.HDocumentTarget;
import net.openl10n.packaging.jpa.document.HResource;
import net.openl10n.packaging.jpa.document.HTextFlow;
import net.openl10n.packaging.jpa.document.HTextFlowTarget;
import net.openl10n.packaging.jpa.project.HProject;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.ContentTarget;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.junit.Before;
import org.junit.Test;

public class HProjectTests extends DbTest{

	private Project project;
	
	@Before
	public void initialize(){
		project = new org.fedorahosted.flies.rest.dto.Project();
		project.setId("myid");
		project.setName("myname");
		project.setSummary("mysummary");
	}
	
	@Test
	public void createProjectFromJaxbObjectWithNoDocuments(){
		EntityManager em = emf.createEntityManager();
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		HProject hProject = new HProject(project);
		
		assertThat("id not transferred", project.getId(), is( hProject.getProjectId() ) );
		assertThat("name not transferred", project.getName(), is( hProject.getName() ) );
		assertThat("summary not transferred", project.getSummary(), is( hProject.getSummary() ) );
		
		em.persist(hProject);
		
		tx.commit();
		
		em.close();
	}
	
	@Test
	public void createProjectFromJaxbObjectWithDocuments(){
		EntityManager em = emf.createEntityManager();
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		Document doc = new Document("/mydo.txt", ContentType.TextPlain, 1, LocaleId.EN_US);
		project.getDocuments().add( new DocumentRef(doc) );
		HProject hProject = new HProject(project);
		
		assertThat("documents not transferred", project.getDocuments().size(), is( 1 ) );
		
		em.persist(hProject);
		
		tx.commit();
		
		em.close();
	}
	
	@Test
	public void persistProjectWithNoDocuments(){
		EntityManager em = emf.createEntityManager();
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		HProject hProject = new HProject(project);
		em.persist(hProject);
		
		tx.commit();
		
		em.close();
	}
	
	@Test
	public void persistProjectWithAnEmptyDocument(){
		EntityManager em = emf.createEntityManager();
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		HProject hProject = new HProject(project);
		em.persist(hProject);
		assertThat("project not managed", hProject.getId(), notNullValue());

		HDocument doc = new HDocument();
		doc.setDocId("/doc1.txt");
		doc.setPath("/");
		doc.setName("doc1.txt");
		doc.setContentType( ContentType.TextPlain );
		doc.setLocale(new LocaleId("en-US"));

		hProject.getDocuments().add(doc);
		
		em.flush();

		assertThat("document not managed", doc.getId(), notNullValue());
		assertThat(hProject.getDocuments().size(), is(1) );

		assertThat(doc.getProject(), nullValue());
		assertThat(doc.getPos(), nullValue());
		em.refresh(doc);
		assertThat(doc.getProject(), is(hProject));
		assertThat(doc.getPos(), is(0));
		
		tx.commit();
		em.close();
	}

	@Test
	public void persistDocumentByItsOwn(){
		EntityManager em = emf.createEntityManager();
		
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		
		HProject hProject = new HProject(project);
		em.persist(hProject);
		assertThat("project not managed", hProject.getId(), notNullValue());

		HDocument doc = new HDocument("/my/doc1.txt", ContentType.TextPlain );

		doc.setPos(0);
		doc.setProject(hProject);
		hProject.getDocuments().add(doc.getPos(),doc);
		em.persist(doc);
		
		tx.commit();
		em.close();
	}
	
}
