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
package org.zanata.dao

import org.apache.commons.lang.StringUtils
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.zanata.common.LocaleId
import org.zanata.model.HLocale
import org.zanata.rest.editor.dto.LocaleSortField
import javax.enterprise.context.RequestScoped

import com.google.common.base.Joiner
import com.google.common.collect.Lists

@RequestScoped
class LocaleDAO : AbstractDAOImpl<HLocale, Long> {

    constructor() : super(HLocale::class.java) {}

    constructor(session: Session) : super(HLocale::class.java, session) {}

    fun findByLocaleId(locale: LocaleId): HLocale? {
        return session.byNaturalId(HLocale::class.java)
                .using("localeId", locale).load()
    }

    fun findBySimilarLocaleId(localeId: LocaleId): List<HLocale> {
        val result = session
                .createQuery("from HLocale l where lower(l.localeId) = :id ")
                .setString("id", localeId.id.toLowerCase())
                .setComment("LocaleDAO.findBySimilarLocaleId").list()
        @Suppress("UNCHECKED_CAST")
        return result as List<HLocale>
    }

    fun findAllActive(): List<HLocale> {
        return findByCriteria(Restrictions.eq("active", true))
    }

    fun findAllActiveAndEnabledByDefault(): List<HLocale> {
        return findByCriteria(Restrictions.eq("active", true),
                Restrictions.eq("enabledByDefault", true))
    }

    override fun findAll(): List<HLocale> {
        return findByCriteria() // Return all of them
    }

    fun find(offset: Int, maxResults: Int, filter: String?,
            sortFields: List<LocaleSortField>?, onlyActive: Boolean): List<HLocale> {
        val query = session
                .createQuery(buildResultSearchQuery(filter, sortFields, onlyActive))
        if (StringUtils.isNotBlank(filter)) {
            query.setString("query", "%" + filter!!.toLowerCase() + "%")
        }
        query.setFirstResult(offset).comment = "LocaleDAO.find"

        if (maxResults != -1) {
            query.setMaxResults(maxResults)
        }
        @Suppress("UNCHECKED_CAST")
        return query.list() as List<HLocale>
    }

    fun countByFind(filter: String?, onlyActive: Boolean): Int {
        val query = session
                .createQuery(buildCountSearchQuery(filter, null, onlyActive))
        if (StringUtils.isNotBlank(filter)) {
            query.setString("query", "%" + filter!!.toLowerCase() + "%")
        }
        query.comment = "LocaleDAO.countByFind"
        val totalCount = query.uniqueResult() ?: return 0
        assert(totalCount is Long)
        return (totalCount as Long).toInt()
    }

    private fun buildCountSearchQuery(filter: String?,
            sortFields: List<LocaleSortField>?, onlyActive: Boolean): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("select count(*) from HLocale")
        queryBuilder.append(buildSearchQuery(filter, sortFields, onlyActive))
        return queryBuilder.toString()
    }

    private fun buildResultSearchQuery(filter: String?,
            sortFields: List<LocaleSortField>?, onlyActive: Boolean): String {
        val queryBuilder = StringBuilder()
        queryBuilder.append("from HLocale")
        queryBuilder.append(buildSearchQuery(filter, sortFields, onlyActive))
        return queryBuilder.toString()
    }

    private fun buildSearchQuery(filter: String?,
            sortFields: List<LocaleSortField>?, onlyActive: Boolean): String {
        val queryBuilder = StringBuilder()
        val hasCondition = StringUtils.isNotBlank(filter) || onlyActive
        if (hasCondition) {
            queryBuilder.append(" where")
        }

        var joinQuery = false
        if (StringUtils.isNotBlank(filter)) {
            joinQuery = true
            queryBuilder.append(" lower(localeId) like :query")
                    .append(" or lower(displayName) like :query")
                    .append(" or lower(nativeName) like :query")
        }
        if (onlyActive) {
            if (joinQuery) {
                queryBuilder.append(" and")
            }
            queryBuilder.append(" active = true")
        }

        if (sortFields != null && !sortFields.isEmpty()) {
            queryBuilder.append(" ORDER BY ")
            val sortQuery = Lists.newArrayList<String>()
            for (sortField in sortFields) {
                val order = if (sortField.isAscending) " ASC" else " DESC"
                sortQuery.add(sortField.entityField + order)
            }
            queryBuilder.append(Joiner.on(", ").join(sortQuery))
        }
        return queryBuilder.toString()
    }
}
