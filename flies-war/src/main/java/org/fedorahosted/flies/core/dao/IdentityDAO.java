package org.fedorahosted.flies.core.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HAccountRole;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.management.PasswordHash;

@Name("identityDAO")
@AutoCreate
public class IdentityDAO {

	@In
	private EntityManager entityManager;
	
	@In 
	private AccountDAO accountDAO;

	public boolean roleExists(String role) {
		return getRole(role) != null;
	}
	
	private HAccountRole getRole(String roleName) {
		Session session = (Session) entityManager.getDelegate();
		return (HAccountRole) session.createCriteria(HAccountRole.class)
			.add( Restrictions.naturalId()
		        .set("name", roleName))
		    .uniqueResult();
	}

	public List<HAccount> listMembers(String role) {
		// FIXME
//		Session session = (Session) entityManager.getDelegate();
//		return session.createCriteria(HAccount.class)
//			.add( Restrictions.naturalId()
//		        .set("name", role))
//		    .list();
//		return Collections.emptyList();

	
	
		Session session = (Session) entityManager.getDelegate();
		return session.createQuery("from HAccount account where :role member of account.roles")
			.setParameter("role", getRole(role))
			.list();
	}

	public boolean createRole(String roleName, String... includesRoles) {
		HAccountRole role = new HAccountRole();
		role.setName(roleName);
		for (String includeRole : includesRoles) {
			role.getGroups().add(getRole(includeRole));
		}
		entityManager.persist(role);
		return true;
	}

	public boolean createUser(String username, String password) {
		HAccount account = new HAccount();
		account.setUsername(username);
		String passwordHash = PasswordHash.instance().generateHash(password);
		account.setPasswordHash(passwordHash);
		entityManager.persist(account);
		return true;
	}

	public void grantRole(String username, String role) {
		HAccount account = accountDAO.getByUsername(username);
		account.getRoles().add(getRole(role));
	}

}
