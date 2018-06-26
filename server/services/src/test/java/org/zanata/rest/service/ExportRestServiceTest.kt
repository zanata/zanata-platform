/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.rest.service

import org.assertj.core.api.Assertions.assertThat
import org.jglue.cdiunit.InRequestScope
import org.junit.Test
import org.zanata.ZanataJpaTest
import org.zanata.common.LocaleId
import org.zanata.dao.AccountDAO
import org.zanata.model.HAccount
import org.zanata.model.HAccountRole
import org.zanata.model.HLocale
import org.zanata.model.HLocaleMember
import org.zanata.model.HPerson
import org.zanata.model.HProject
import org.zanata.model.HProjectIteration
import org.zanata.model.HProjectLocaleMember
import org.zanata.model.HProjectMember
import org.zanata.model.LocaleRole
import org.zanata.model.ProjectRole
import org.zanata.seam.security.AltCurrentUser
import org.zanata.service.GraphQLService
import org.zanata.test.Descriptions.describe
import org.zanata.util.toMap
import java.net.HttpURLConnection.HTTP_OK

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com),
 * Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
class ExportRestServiceTest : ZanataJpaTest() {

    @Test
    @InRequestScope
    fun exportUserDataTest() {

        val locale = HLocale(LocaleId.DE)
        getEm().persist(locale)
        val myProject = HProject().apply {
            slug = "myProject"
            name = "My Project"
        }
        myProject.addIteration(HProjectIteration().apply { slug = "master" })
        val someProject = HProject().apply {
            slug = "someProject"
            name = "Some Project"
        }
        someProject.projectIterations = listOf(
                HProjectIteration().apply { slug = "master" },
                HProjectIteration().apply { slug = "release" })

        val hAccount = HAccount().apply {
            username = "testAccount"
            apiKey = "12345678901234567890123456789012"
            isEnabled = true
            roles.add(HAccountRole().apply { name = "user" })
            person = HPerson()
            person.apply {
                name = "Person Name"
                email = "person@example.com"
                languageTeamMemberships.add(
                        HLocaleMember(person, locale, true, true, true))
                projectLocaleMemberships.addAll(listOf(
                        HProjectLocaleMember(someProject, locale, person, LocaleRole.Translator),
                        HProjectLocaleMember(someProject, locale, person, LocaleRole.Reviewer)))
                projectMemberships.addAll(listOf(
                        HProjectMember(someProject, person, ProjectRole.TranslationMaintainer),
                        HProjectMember(myProject, person, ProjectRole.Maintainer)))
            }
        }

        val accountDAO = AccountDAO(session)
        accountDAO.makePersistent(hAccount)

        val graphQLService = GraphQLService(accountDAO).apply { postConstruct() }
        val currentUser = AltCurrentUser().apply { account = hAccount }
        val exportRestService = ExportRestService(currentUser, graphQLService)

        val response = exportRestService.exportUserData()
        assertThat(response.status).describedAs(describe { response.entity }).isEqualTo(HTTP_OK)
        val json = response.entity.toString()
        val jsonMap = toMap(json)

//        println(org.zanata.util.toJson(json, pretty = true))

        assertThat(jsonMap).containsKey("account")

        val accountMap = jsonMap["account"] as Map<*, *>
        val flatAccount = flatten(accountMap)
        println(flatAccount)

        assertThat(flatAccount)
                .containsEntry("username", "testAccount")
                .containsEntry("enabled", true)
                .containsEntry("roles[0].name", "user")
                .containsEntry("person.name", "Person Name")
                .containsEntry("person.email", "person@example.com")
                .containsEntry("person.languageTeamMemberships[0].locale.localeId", "de")
                .containsEntry("person.languageTeamMemberships[0].isCoordinator", true)
                .containsEntry("person.languageTeamMemberships[0].isReviewer", true)
                .containsEntry("person.languageTeamMemberships[0].isTranslator", true)
                .doesNotContainKey("apiKey")
                .doesNotContainKey("passwordHash")

        val person = accountMap["person"] as Map<*, *>

        // note that memberships are not ordered, so tests which depend on order must use sorting

        val projectLocaleMemberships = (person["projectLocaleMemberships"] as List<*>)
                .sortedBy(this::role)
        assertThat(projectLocaleMemberships).hasSize(2)
        val plMembers = projectLocaleMemberships.map { flatten(it as Map<*, *>) }
        assertThat(plMembers[0])
                .containsEntry("role", "Reviewer")
                .containsEntry("project.slug", "someProject")
                .containsEntry("locale.localeId", "de")
        assertThat(plMembers[1])
                .containsEntry("role", "Translator")
                .containsEntry("project.slug", "someProject")
                .containsEntry("locale.localeId", "de")

        val projectMemberships = (person["projectMemberships"] as List<*>)
                .sortedBy(this::role)
        assertThat(projectMemberships).hasSize(2)
        val pMembers = projectMemberships.map { flatten(it as Map<*, *>) }
        assertThat(pMembers[0])
                .containsEntry("role", "Maintainer")
                .containsEntry("project.slug", "myProject")
                .containsEntry("project.name", "My Project")
                .containsEntry("project.projectIterations[0].slug", "master")
        assertThat(pMembers[1])
                .containsEntry("role", "TranslationMaintainer")
                .containsEntry("project.slug", "someProject")
                .containsEntry("project.name", "Some Project")

        val translationMaintainerIters =
                (projectMemberships[1]!!.get("project", "projectIterations") as List<*>).sortedBy(this::slug)
        assertThat(translationMaintainerIters).extracting("slug").containsExactlyInAnyOrder("master", "release")
    }

    /*
     * Treats the object as a nested Map (JSON style) and extracts elements according to the keys
     */
    private fun Any.get(vararg keys: String): Any? {
        var next: Any? = this
        for (k in keys) {
            val map = next!! as Map<*, *>
            next = map[k]
        }
        return next
    }

    private fun role(map: Any?) = ((map as Map<*, *>)["role"]).toString()
    private fun slug(map: Any?) = ((map as Map<*, *>)["slug"]).toString()

    /*
     * Don't use this in production as is, because it doesn't escape special characters like ".[]".
     * See json-flattener for a strategy which handles ["keys.with.full.stops"]
     */
    private fun flatten(map: Map<*, *>): Map<String, Any?> {
        return map.flatMap { e -> flatten(e.key.toString(), e.value) }.toMap()
    }

    private fun flatten(k: String, v: Any?): List<Pair<String, Any?>> {
        return when (v) {
            is Map<*, *> -> v.flatMap { flatten("$k.${it.key}", it.value) }
            is List<*> -> v.withIndex().flatMap { flatten("$k[${it.index}]", it.value) }
            else -> listOf(k to v)
        }
    }
}
