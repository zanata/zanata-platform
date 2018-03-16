/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.dao;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.zanata.exception.EntityMissingException;
import org.zanata.jpa.FullText;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;

/**
 * Data Access Object for Translation Memory and related entities.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("transMemoryDAO")
@RequestScoped
public class TransMemoryDAO extends AbstractDAOImpl<TransMemory, Long> {

    private static final long serialVersionUID = -6140351094748357541L;
    @Inject @FullText
    private FullTextSession session;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Inject
    private UserTransaction userTransaction;

    public TransMemoryDAO() {
        super(TransMemory.class);
    }

    public TransMemoryDAO(Session session) {
        super(TransMemory.class, session);
    }

    public Optional<TransMemory> getBySlug(@Nonnull String slug) {
        if (!StringUtils.isEmpty(slug)) {
            TransMemory tm =
                    getSession().byNaturalId(TransMemory.class)
                            .using("slug", slug).load();
            return Optional.ofNullable(tm);
        }
        return Optional.empty();
    }

    /**
     * Deletes the contents for a single translation memory. Because TMs could
     * potentially contain very large numbers of translation units, this process
     * is broken into multiple transactions and could take a long time.
     *
     * @param slug
     *            Translation memory identifier to clear.
     * @return the number of trans units deleted (not including variants)
     */
    public int deleteTransMemoryContents(@Nonnull String slug) {
        int totalDeleted = 0;
        Optional<TransMemory> tm = getBySlug(slug);
        if (!tm.isPresent()) {
            throw new EntityMissingException("Translation memory " + slug
                    + " was not found.");
        }

        final int batchSize = 100;
        int deleted;
        do {
            try {
                userTransaction.begin();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            @SuppressWarnings("unchecked")
            List<TransMemoryUnit> toRemove =
                    session.createQuery(
                            "from TransMemoryUnit tu where tu.translationMemory = :tm")
                            .setParameter("tm", tm.get()).setFirstResult(0)
                            .setMaxResults(batchSize).list();

            // Remove each batch (Takes advantage of CASCADE deletes on the db)
            for (TransMemoryUnit tmu : toRemove) {
                session.purge(TransMemoryUnit.class, tmu);
            }

            if (toRemove.size() > 0) {
                deleted =
                        session.createQuery(
                                "delete TransMemoryUnit tu where tu in :tus")
                                .setParameterList("tus", toRemove)
                                .executeUpdate();
                totalDeleted += deleted;
            } else {
                deleted = 0;
            }

            try {
                userTransaction.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } while (deleted == batchSize);
        return totalDeleted;
    }

    public @Nullable
    TransMemoryUnit findTranslationUnit(@Nonnull String tmSlug,
            @Nonnull String uniqueId) {
        return (TransMemoryUnit) getSession()
                .createQuery(
                        "from TransMemoryUnit tu where tu.uniqueId = :uniqueId and tu.translationMemory.slug = :tmSlug")
                .setString("uniqueId", uniqueId).setString("tmSlug", tmSlug)
                .setCacheable(false).uniqueResult();
    }

    public long getTranslationMemorySize(@Nonnull String tmSlug) {
        return (Long) getSession()
                .createQuery(
                        "select count(tu) from TransMemoryUnit tu where tu.translationMemory.slug = :tmSlug")
                .setString("tmSlug", tmSlug).setCacheable(true).uniqueResult();
    }
}
