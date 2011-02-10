/**
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. You should have received a copy of GPLv2 along with this
 * software; if not, see http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * 
 * Red Hat trademarks are not licensed under GPLv2. No permission is granted to
 * use or replicate Red Hat trademarks that are incorporated in this software or
 * its documentation.
 */
package net.openl10n.flies.dao;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

import net.openl10n.flies.model.HAccount;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.PasswordHash;
import org.jboss.seam.util.Hex;

@Name("accountDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class AccountDAO extends AbstractDAOImpl<HAccount, Long>
{
   public AccountDAO()
   {
      super(HAccount.class);
   }

   public AccountDAO(Session session)
   {
      super(HAccount.class, session);
   }

   public HAccount getByUsername(String username)
   {
      return (HAccount) getSession().createCriteria(HAccount.class).add(Restrictions.naturalId().set("username", username)).uniqueResult();
   }

   public HAccount getByApiKey(String apikey)
   {
      return (HAccount) getSession().createCriteria(HAccount.class).add(Restrictions.eq("apiKey", apikey)).uniqueResult();
   }

   public void createApiKey(HAccount account)
   {
      String username = account.getUsername();
      String apikey = createSaltedApiKey(username);
      account.setApiKey(apikey);
   }

   public static String createSaltedApiKey(String username)
   {
      try
      {
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

      }
      catch (Exception exc)
      {
         throw new RuntimeException(exc);
      }

   }

   public HAccount create(String username, String password, boolean enabled)
   {
      HAccount account = new HAccount();
      account.setUsername(username);
      // TODO add a @PasswordSalt field to HAccount
      // otherwise, Seam uses the @UserPrincipal field as salt
      String saltPhrase = username;
      @SuppressWarnings("deprecation")
      String passwordHash = PasswordHash.instance().generateSaltedHash(password, saltPhrase, PasswordHash.ALGORITHM_MD5);
      account.setPasswordHash(passwordHash);
      account.setEnabled(enabled);
      makePersistent(account);
      return account;
   }

   // @SuppressWarnings("unchecked")
   // public List<HAccount> searchQuery(String searchQuery) throws
   // ParseException
   // {
   // log.info("start searching {0}", searchQuery);
   // TermQuery tq = new TermQuery(new Term("username", searchQuery));
   // EdgeNGramTokenFilter

   // FullTextQuery fullTextQuery = ((FullTextEntityManager)
   // entityManager).createFullTextQuery(tq, HAccount.class);
   // return fullTextQuery.getResultList();
   // }

   @SuppressWarnings("unchecked")
   public List<HAccount> searchQuery(String searchQuery)
   {
      String userName = searchQuery + "%";
      org.hibernate.Query query = getSession().getNamedQuery("getSearchLogin").setString("username", userName);
      return query.list();
   }

}
