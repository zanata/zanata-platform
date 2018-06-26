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
package org.zanata.model

import com.ibm.icu.util.ULocale
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.annotations.types.GraphQLType
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.zanata.common.LocaleId
import org.zanata.model.type.LocaleIdType
import javax.persistence.Cacheable
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull
import java.io.Serializable
import java.util.HashSet

/**
 * Database entity which represents a locale, which can be used as a source
 * or target locale for translations, the plural forms for the language,
 * and human-readable names for the locale. Based on RFC 3066 locale IDs as
 * used in ICU4J's ULocale.
 */
@Entity
@Cacheable
@TypeDef(name = "localeId", typeClass = LocaleIdType::class)
@GraphQLType(name = "Locale")
class HLocale : ModelEntityBase, Serializable, HasUserFriendlyToString {
    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @get:NaturalId
    @get:NotNull
    @get:Type(type = "localeId")
    @GraphQLQuery(name = "localeId", description = "localeId")
    var localeId: LocaleId

    var isActive: Boolean = false
    var isEnabledByDefault: Boolean = false

    var supportedProjects: Set<HProject>? = null
        @ManyToMany
        @JoinTable(
                name = "HProject_Locale",
                joinColumns = [JoinColumn(name = "localeId")],
                inverseJoinColumns = [JoinColumn(name = "projectId")])
        get() {
            if (field == null) field = HashSet()
            return field
        }
    var supportedIterations: Set<HProjectIteration>? = null
        @ManyToMany
        @JoinTable(
                name = "HProjectIteration_Locale",
                joinColumns = [JoinColumn(name = "localeId")],
                inverseJoinColumns = [JoinColumn(name = "projectIterationId")])
        get() {
            if (field == null) field = HashSet()
            return field
        }
    var members: Set<HLocaleMember>? = null
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "id.supportedLanguage")
        @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
        get() {
            if (field == null) field = HashSet()
            return field
        }

    var pluralForms: String? = null

    @GraphQLQuery(name = "name", description = "name of the language")
    var displayName: String? = null

    var nativeName: String? = null

    constructor(localeId: LocaleId) {
        this.localeId = localeId
    }

    constructor(localeId: LocaleId, enabledByDefault: Boolean,
            active: Boolean) {
        this.localeId = localeId
        this.isEnabledByDefault = enabledByDefault
        this.isActive = active
    }

    /**
     * Gets the name of the locale according to that locale, either from the
     * database record if specified, otherwise from ICU4J.
     */
    fun retrieveNativeName(): String? {
        if (nativeName.isNullOrEmpty()) return retrieveDefaultNativeName()
        return nativeName
    }

    /**
     * Gets the name of the locale according to the server's default locale,
     * either from the database record if specified, otherwise from ICU4J.
     */
    // TODO these 'retrieve' methods are unconventional, replace them with
    //      getters so devs don't waste time trying to use 'get' methods that
    //      don't work properly.
    // FIXME this exact thing just wasted another 15 mins of my time - damason
    fun retrieveDisplayName(): String {
        if (displayName.isNullOrEmpty()) return retrieveDefaultDisplayName()
        return displayName!!
    }

    /**
     * Gets the default name of the locale according to that locale, from ICU4J.
     */
    fun retrieveDefaultNativeName(): String = asULocale().getDisplayName(asULocale())

    /**
     * Gets the name of the locale according to the server's default locale,
     * from ICU4J.
     */
    fun retrieveDefaultDisplayName(): String = asULocale().displayName

    /**
     * Gets the ICU4J ULocale for this locale.
     */
    fun asULocale() = ULocale(this.localeId.id)

    override fun userFriendlyToString() = "Locale(id=%s, name=%s)".format(localeId,
                retrieveDisplayName())

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is HLocale) return false
        if (!other.canEqual(this)) return false
        if (this.localeId != other.localeId) return false
        return true
    }

    override fun hashCode(): Int {
        return 59 + localeId.hashCode()
    }

    private fun canEqual(other: Any) = other is HLocale

    override fun toString() = "HLocale(localeId=$localeId)"

    companion object {
        private const val serialVersionUID = 1L
    }
}
