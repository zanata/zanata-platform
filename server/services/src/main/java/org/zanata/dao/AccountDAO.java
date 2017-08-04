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
package org.zanata.dao;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import org.zanata.model.HAccount;
import org.zanata.util.PasswordUtil;
import com.google.common.base.Strings;

@RequestScoped
public class AccountDAO extends AbstractDAOImpl<HAccount, Long> {
    public static final String REGION = "Account";
    public AccountDAO() {
        super(HAccount.class);
    }

    public AccountDAO(Session session) {
        super(HAccount.class, session);
    }

    public @Nullable HAccount getByUsername(String username) {
        Criteria cr = getSession().createCriteria(HAccount.class);
        cr.add(Restrictions.eq("username", username));
        cr.setCacheRegion(REGION).setCacheable(true).setComment("AccountDAO.getByUsername");
        return (HAccount) cr.uniqueResult();
    }

    public Optional<HAccount> tryGetByUsername(@Nonnull String username) {
        return Optional.ofNullable(getByUsername(username));
    }

    public HAccount getByEmail(String email) {
        return (HAccount) getSession()
            .createQuery(
                "from HAccount acc where acc.person.email = :email")
            .setString("email", email)
            .setComment("AccountDAO.getByEmail").uniqueResult();
    }

    public HAccount getByUsernameAndEmail(String username, String email) {
        return (HAccount) getSession()
                .createQuery(
                        "from HAccount acc where acc.username = :username and acc.person.email = :email")
                .setString("username", username).setString("email", email)
                .setComment("AccountDAO.getByUsernameAndEmail").uniqueResult();
    }

    public HAccount getByApiKey(String apikey) {
        return (HAccount) getSession().createCriteria(HAccount.class)
                .add(Restrictions.eq("apiKey", apikey)).uniqueResult();
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

            return new String(PasswordUtil.encodeHex(digest));

        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

    }

    public HAccount create(String username, String password, boolean enabled) {
        HAccount account = new HAccount();
        account.setUsername(username);
        // TODO add a @PasswordSalt field to HAccount
        // otherwise, Seam uses the @UserPrincipal field as salt
        String saltPhrase = username;
        @SuppressWarnings("deprecation")
        String passwordHash =
                PasswordUtil.generateSaltedHash(password,
                        saltPhrase);
        account.setPasswordHash(passwordHash);
        account.setEnabled(enabled);
        makePersistent(account);
        return account;
    }

    // @SuppressWarnings("unchecked")
    // public List<HAccount> searchQuery(String searchQuery) throws
    // ParseException
    // {
    // log.info("start searching {}", searchQuery);
    // TermQuery tq = new TermQuery(new Term("username", searchQuery));
    // EdgeNGramTokenFilter

    // FullTextQuery fullTextQuery = ((FullTextEntityManager)
    // entityManager).createFullTextQuery(tq, HAccount.class);
    // return fullTextQuery.getResultList();
    // }

    @SuppressWarnings("unchecked")
    // TODO: use hibernate search
            public
            List<HAccount> searchQuery(String searchQuery, int maxResults, int firstResult) {
        String userName = "%" + searchQuery + "%";
        Query query =
                getSession().createQuery(
                        "from HAccount as a where lower(a.username) like lower(:username)");
        query.setParameter("username", userName);
        if(maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        query.setFirstResult(firstResult);
        query.setComment("AccountDAO.searchQuery/username");
        return query.list();
    }

    public List<String> getUserNames(String filter, int offset, int maxResults) {
        StringBuilder queryBuilder =
                new StringBuilder("select username from HAccount ");
        if (!StringUtils.isEmpty(filter)) {
            queryBuilder.append("where lower(username) like :filter");
        }
        Query query = getSession().createQuery(queryBuilder.toString());
        if (!StringUtils.isEmpty(filter)) {
            query.setParameter("filter", "%" + filter.toLowerCase() + "%");
        }
        query.setMaxResults(maxResults);
        query.setFirstResult(offset);
        query.setCacheable(true);
        query.setComment("accountDAO.getUserNames");
        return (List<String>) query.list();
    }

    public int getUserCount(String filter) {
        StringBuilder queryBuilder = new StringBuilder("select count(*) from HAccount ");
        if (!StringUtils.isEmpty(filter)) {
            queryBuilder.append("where lower(username) like :filter");
        }
        Query query = getSession().createQuery(queryBuilder.toString());
        if (!StringUtils.isEmpty(filter)) {
            query.setParameter("filter", "%" + filter.toLowerCase() + "%");
        }
        query.setCacheable(true);
        query.setComment("accountDAO.getUserCount");
        Long totalCount = (Long) query.uniqueResult();
        if (totalCount == null) {
            return 0;
        }
        return totalCount.intValue();
    }

    public HAccount getByCredentialsId(String credentialsId) {
        Query query =
                getSession()
                        .createQuery(
                                "select c.account from HCredentials c where c.user = :id");
        query.setParameter("id", credentialsId);
        query.setComment("AccountDAO.getByCredentialsId");
        return (HAccount) query.uniqueResult();
    }

    /**
     * Returns all accounts merged into the another one.
     *
     * @param mergedInto
     *            The account into which all returned accounts were merged.
     * @return A list of accounts that in the past were merged into the given
     *         account.
     */
    public List<HAccount> getAllMergedAccounts(HAccount mergedInto) {
        Query query =
                getSession().createQuery(
                        "from HAccount as a where a.mergedInto = :mergedInto");
        query.setParameter("mergedInto", mergedInto);
        query.setComment("AccountDAO.getAllMergedAccounts");
        return (List<HAccount>) query.list();
    }
}
