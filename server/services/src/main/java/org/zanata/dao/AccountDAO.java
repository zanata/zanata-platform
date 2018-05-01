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

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import org.zanata.model.HAccount;
import org.zanata.util.PasswordUtil;

@RequestScoped
public class AccountDAO extends AbstractDAOImpl<HAccount, Long> {
    public static final String REGION = "Account";
    private static final long serialVersionUID = -2710311827560778973L;

    public AccountDAO() {
        super(HAccount.class);
    }

    public AccountDAO(Session session) {
        super(HAccount.class, session);
    }

    public @Nullable HAccount getEnabledByUsername(String username) {
        Criteria cr = getSession().createCriteria(HAccount.class);
        cr.add(Restrictions.eq("username", username));
        cr.add(Restrictions.eq("enabled", true));
        cr.setCacheRegion(REGION).setCacheable(true).setComment("AccountDAO.getEnabledByUsername");
        return (HAccount) cr.uniqueResult();
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
        String apikey = generateAPIKey();
        account.setApiKey(apikey);
    }

    protected static String generateAPIKey() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return new String(PasswordUtil.encodeHex(bytes));
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

    // TODO: use hibernate search
    public List<HAccount> searchQuery(String searchQuery, int maxResults,
            int firstResult) {
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
        @SuppressWarnings("unchecked")
        List<HAccount> list = query.list();
        return list;
    }

    public List<String> getUserNames(String filter, int offset, int maxResults) {
        Query query = createFilteredQuery(
                "select distinct acc.username from HAccount acc ", filter)
                .setMaxResults(maxResults)
                .setFirstResult(offset)
                .setComment("accountDAO.getUserNames");
        @SuppressWarnings("unchecked")
        List<String> list = query.list();
        return list;
    }

    public int getUserCount(String filter) {
        return ((Long) createFilteredQuery(
                "select count(*) from HAccount acc ", filter)
                .setComment("accountDAO.getUserCount").uniqueResult())
                .intValue();
    }

    private Query createFilteredQuery(String queryBase, String filter) {
        if (!StringUtils.isEmpty(filter)) {
            queryBase += "inner join acc.person as person " +
                    "where lower(acc.username) like :filter " +
                    "OR lower(person.email) like :filter " +
                    "OR lower(person.name) like :filter";
        }
        Query query = getSession().createQuery(queryBase).setCacheable(true);
        if (!StringUtils.isEmpty(filter)) {
            query.setParameter("filter", "%" + filter.toLowerCase() + "%");
        }
        return query;
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
        @SuppressWarnings("unchecked")
        List<HAccount> list = query.list();
        return list;
    }
}
