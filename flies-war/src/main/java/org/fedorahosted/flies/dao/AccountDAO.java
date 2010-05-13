package org.fedorahosted.flies.dao;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.model.HAccount;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.util.Hex;

@Name("accountDAO")
@AutoCreate
public class AccountDAO {

	@In
	EntityManager entityManager;
	
	public AccountDAO() {
	}
	
	public AccountDAO(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public HAccount getByUsername(String username){
		Session session = (Session) entityManager.getDelegate();
		return (HAccount) session.createCriteria(HAccount.class)
			.add( Restrictions.naturalId()
		        .set("username", username))
		    .uniqueResult();
	}

	public HAccount getByApiKey(String apikey) {
		Session session = (Session) entityManager.getDelegate();
		return (HAccount) session.createCriteria(HAccount.class).add(
				Restrictions.eq("apiKey", apikey)).uniqueResult();
	}

	public void createApiKey(HAccount account) {
		String username = account.getUsername();
		String apikey = createSaltedApiKey(username);
		account.setApiKey(apikey);
	}

	public static String createSaltedApiKey(String username) {
		try {
			byte[] salt = new byte[16];
			SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] name = username.getBytes("UTF-8");

			// add salt
			byte[] salted = new byte[name.length + salt.length];
			System.arraycopy(name, 0, salted, 0, name.length);
			System.arraycopy(salt, 0, salted, name.length, salt.length);

			// generate md5 digest
			md5.reset();
			byte[] digest = md5.digest(salted);

			return new String(Hex.encodeHex(digest));
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}

	}
}
