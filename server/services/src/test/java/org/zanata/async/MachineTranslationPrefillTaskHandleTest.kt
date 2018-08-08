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


import org.junit.Test
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class MachineTranslationPrefillTaskHandleTest {

    @Test
    fun testProgress() {
        val handle = MachineTranslationPrefillTaskHandle { "12345" }
        handle.setTotalUnits(200)
        assertThat(handle.increaseProgress(50)).isEqualTo(25)
        assertThat(handle.increaseProgress(1)).isEqualTo(25)
        assertThat(handle.increaseProgress(9)).isEqualTo(30)
        assertThat(handle.increaseProgress(140)).isEqualTo(100)
        assertThat(handle.increaseProgress(50)).isEqualTo(100)
    }

    @Test
    fun testToString() {
        val handle = MachineTranslationPrefillTaskHandle { "12345" }
        handle.triggeredBy = "Leeloo"
        handle.maxProgress = 100
        handle.setTargetVersion("zanata-server-master")
        handle.setTotalUnits(100)
        handle.increaseProgress(50)
        assertThat(handle.triggeredBy).isEqualTo("Leeloo")
        assertThat(handle.toString()).isEqualTo("""
            |MachineTranslationPrefillTaskHandle{
            |targetVersion=zanata-server-master,
            | currentProgress=50,
            | maxProgress=100,
            | triggeredBy=Leeloo}"""
                .trimMargin().replace("\n", ""))
    }

}
