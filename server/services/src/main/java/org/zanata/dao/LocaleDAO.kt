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
@file:Suppress("TooManyFunctions")
package org.zanata.dao

import com.google.common.base.Joiner
import com.google.common.collect.Lists
import org.apache.commons.lang3.StringUtils
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Restrictions
import org.hibernate.transform.ResultTransformer
import org.zanata.common.EntityStatus
import org.zanata.common.LocaleId
import org.zanata.model.HLocale
import org.zanata.rest.editor.dto.LocaleSortField
import java.util.*
import javax.enterprise.context.RequestScoped

@RequestScoped
class LocaleDAO : AbstractDAOImpl<HLocale, Long> {

    constructor() : super(HLocale::class.java) {}

    constructor(session: Session) : super(HLocale::class.java, session) {}

    fun findByLocaleId(locale: LocaleId): HLocale? = session.byNaturalId(HLocale::class.java)
                .using("localeId", locale).load()

    fun findBySimilarLocaleId(localeId: LocaleId): List<HLocale> {
        val result = session
                .createQuery("from HLocale l where lower(l.localeId) = :id ")
                .setString("id", localeId.id.toLowerCase())
                .setComment("LocaleDAO.findBySimilarLocaleId").list()
        @Suppress("UNCHECKED_CAST")
        return result as List<HLocale>
    }

    fun findAllActive(): List<HLocale> = findByCriteria(Restrictions.eq("active", true))

    fun findAllActiveAndEnabledByDefault(): List<HLocale> = findByCriteria(Restrictions.eq("active", true),
                Restrictions.eq("enabledByDefault", true))

    override fun findAll(): List<HLocale> = findByCriteria()

    fun find(offset: Int, maxResults: Int, filter: String?,
            sortFields: List<LocaleSortField>?, onlyActive: Boolean): List<HLocale> {
        val query = session
                .createQuery(buildResultSearchQuery(filter, sortFields, onlyActive))
        if (StringUtils.isNotBlank(filter)) {
            val escapeFilter = escapeQuery(filter)
            query.setString("query", "%$escapeFilter%")
        }
        query.setFirstResult(offset).comment = "LocaleDAO.find"

        if (maxResults != -1) {
            query.setMaxResults(maxResults)
        }
        @Suppress("UNCHECKED_CAST")
        return query.list() as List<HLocale>
    }

    fun getAllSourceLocalesAndDocCount(): Map<HLocale, Int> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("select doc.locale as locale, count(*) as count from HDocument doc ")
                .append("where doc.obsolete = false ")
                .append("and doc.projectIteration.status<>:OBSOLETE ")
                .append("and doc.projectIteration.project.status<>:OBSOLETE ")
                .append("group by doc.locale")

        val query = session.createQuery(queryBuilder.toString())
                .setParameter("OBSOLETE", EntityStatus.OBSOLETE)
                .setComment("LocaleDAO.getTranslationLocales")

        return processGetSourceLocalesAndDocCount(query)
    }

    fun getProjectSourceLocalesAndDocCount(
            projectSlug: String): Map<HLocale, Int> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("select doc.locale as locale, count(*) as count from HDocument doc ")
                .append("where doc.obsolete = false ")
                .append("and doc.projectIteration.status<>:OBSOLETE ")
                .append("and doc.projectIteration.project.status<>:OBSOLETE ")
                .append("and doc.projectIteration.project.slug =:projectSlug ")
                .append("group by doc.locale")

        val query = session.createQuery(queryBuilder.toString())
                .setParameter("projectSlug", projectSlug)
                .setParameter("OBSOLETE", EntityStatus.OBSOLETE)
                .setComment("ProjectDAO.getTranslationLocales")
        return processGetSourceLocalesAndDocCount(query)
    }

    fun getProjectVersionSourceLocalesAndDocCount(
            projectSlug: String, versionSlug: String): Map<HLocale, Int> {
        val queryBuilder = StringBuilder()
        queryBuilder.append("select doc.locale as locale, count(*) as count from HDocument doc ")
                .append("where doc.obsolete = false ")
                .append("and doc.projectIteration.status<>:OBSOLETE ")
                .append("and doc.projectIteration.slug =:versionSlug ")
                .append("and doc.projectIteration.project.slug =:projectSlug ")
                .append("group by doc.locale")

        val query = session.createQuery(queryBuilder.toString())
                .setParameter("versionSlug", versionSlug)
                .setParameter("projectSlug", projectSlug)
                .setParameter("OBSOLETE", EntityStatus.OBSOLETE)
                .setComment("ProjectIterationDAO.getTranslationLocales")

        return processGetSourceLocalesAndDocCount(query)
    }

    fun processGetSourceLocalesAndDocCount(query: Query): Map<HLocale, Int> {
        @Suppress("UNCHECKED_CAST")
        val list = query.setResultTransformer(LocalesAndDocCountTransformer()).list()
                as List<Map.Entry<HLocale, Int>>
        var map = HashMap<HLocale, Int>()
        for (entry in list) {
            map.put(entry.key, entry.value)
        }
        return map
    }

    fun countByFind(filter: String?, onlyActive: Boolean): Int {
        val query = session
                .createQuery(buildCountSearchQuery(filter, null, onlyActive))
        if (StringUtils.isNotBlank(filter)) {
            val escapeFilter = escapeQuery(filter)
            query.setString("query", "%$escapeFilter%")
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
            queryBuilder.append(" lower(localeId) like lower(:query) escape '!'")
                    .append(" or lower(displayName) like lower(:query) escape '!'")
                    .append(" or lower(nativeName) like lower(:query) escape '!'")
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

    class LocalesAndDocCountTransformer : ResultTransformer {
        private companion object {
            const val LOCALE_COL = "locale"
            const val COUNT_COL = "count"
        }
        override fun transformTuple(tuple: Array<Any>,
                                    aliases: Array<String>): Map.Entry<HLocale, Int> {
            var hLocale: HLocale? = null
            var count: Int? = null
            var i = 0
            val aliasesLength = aliases.size
            while (i < aliasesLength) {
                val columnName = aliases[i]
                if (columnName == LOCALE_COL) {
                    hLocale = tuple[i] as HLocale
                } else if (columnName == COUNT_COL) {
                    count = Math.toIntExact(tuple[i] as Long)
                }
                i++
            }
            return AbstractMap.SimpleEntry<HLocale, Int>(hLocale, count)
        }

        override fun transformList(collection: List<*>): List<*> {
            return collection
        }
    }
}
