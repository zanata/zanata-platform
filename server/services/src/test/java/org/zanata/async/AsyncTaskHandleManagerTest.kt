/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.zanata.async.handle.CopyTransTaskHandle
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle
import org.zanata.async.handle.MergeTranslationsTaskHandle
import org.zanata.async.handle.TransMemoryMergeTaskHandle
import org.zanata.security.ZanataIdentity
import javax.inject.Inject

import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.`when`

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class AsyncTaskHandleManagerTest {

    @Inject
    private lateinit var manager: AsyncTaskHandleManager
    @Mock
    private lateinit var identity: ZanataIdentity

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        manager = AsyncTaskHandleManager()
        val handle = MachineTranslationPrefillTaskHandle { "first" }.apply {
            triggeredBy = "john"
            taskName = "John's Task"
        }
        val handle2 = TransMemoryMergeTaskHandle().apply {
            keyId = "second"
            triggeredBy = "jane"
            taskName = "Jane's Task"
        }
        val handle3 = MergeTranslationsTaskHandle { "third" }.apply {
            triggeredBy = "john"
            taskName = "John's Task"
        }
        val handle4 = CopyTransTaskHandle().apply {
            keyId = "fourth"
            triggeredBy = "jane"
            taskName = "Jane's Task"
        }

        manager.registerTaskHandle(handle)
        manager.registerTaskHandle(handle2)
        manager.registerTaskHandle(handle3)
        manager.registerTaskHandle(handle4)
        assertThat(manager.allHandles).hasSize(4)
    }

    @Test
    fun returnOnlyTheUsersHandles() {
        `when`<String>(identity.accountUsername).thenReturn("jane")
        val tasks = manager.getTasksFor(identity)
        assertThat(tasks)
                .describedAs("Only two tasks are returned")
                .hasSize(2)
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val handle = iterator.next()
            assertThat(handle.taskName)
                    .describedAs("Only Jane's tasks are returned")
                    .isEqualTo("Jane's Task")
        }
    }
}
