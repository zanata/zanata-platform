/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

import org.junit.Test;
import org.zanata.common.ActivityType;
import org.zanata.common.ProjectType;

import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ActivityTest {

    @Test
    public void testEquals() {
        HDocument document = new HDocument();
        document.setId(555666777L);
        HPerson actorA = new HPerson();
        actorA.setName("lara");
        actorA.setEmail("lara@test.com");
        actorA.setId(321654987L);

        HPerson actorB = new HPerson();
        actorB.setId(987654321L);
        actorB.setName("aloy");
        actorB.setEmail("aloy@test.com");

        HProjectIteration versionA = new HProjectIteration();
        versionA.setId(1234567890L);
        HProjectIteration versionB = new HProjectIteration();
        versionB.setId(5678901234L);
        versionB.setProjectType(ProjectType.Utf8Properties);

        Activity activity = new Activity();

        assertThat(activity.equals(new Activity())).isTrue();

        activity = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);

        assertThat(activity.equals(new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000))).isTrue();

        assertEqualsFalse(activity, new Activity(actorB, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000));
        assertEqualsFalse(activity, new Activity(actorA, document, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000));
        assertEqualsFalse(activity, new Activity(actorA, versionA, document,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000));
        assertEqualsFalse(activity, new Activity(actorA, versionA, versionA,
                ActivityType.REVIEWED_TRANSLATION, 9000));
        assertEqualsFalse(activity, new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9001));
    }

    private void assertEqualsFalse(Activity a, Activity b) {
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    public void hashcodeTest() {
        HDocument document = new HDocument();
        document.setId(555666777L);
        HPerson actorA = new HPerson();
        HPerson actorB = new HPerson();
        actorB.setName("aloy");

        HProjectIteration versionA = new HProjectIteration();
        versionA.setId(1234567890L);
        HProjectIteration versionB = new HProjectIteration();
        versionB.setId(5678901234L);
        versionB.setProjectType(ProjectType.Utf8Properties);

        int hashCode1 = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000).hashCode();
        int hashCode2 = new Activity(actorB, versionB, versionB,
                ActivityType.REVIEWED_TRANSLATION, 1000).hashCode();
        assertThat(hashCode1 == hashCode2).isFalse();
        hashCode2 = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000).hashCode();
        assertThat(hashCode1 == hashCode2).isTrue();
    }

    @Test
    public void updateActivity() throws Exception {
        HDocument document = new HDocument();
        document.setId(555666777L);
        HPerson person = new HPerson();

        HProjectIteration version = new HProjectIteration();
        version.setId(1234567890L);

        Activity activity = new Activity(person, version, version,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        activity.setCreationDate(new Date(0L));

        Field f = activity.getClass().getDeclaredField("approxTime");
        f.setAccessible(true);
        f.set(activity, new Date(0L));

        activity.updateActivity(new Date(10000L), document, 55);

        //TODO: assertThat(activity.getApproxTime()).isEqualTo(new Date(?));
        assertThat(activity.getActor()).isEqualTo(person);
        assertThat(activity.getWordCount()).isEqualTo(9055);
        assertThat(activity.getEventCount()).isEqualTo(2);
        assertThat(activity.getLastTargetType()).isEqualTo(document.getEntityType());
    }

    @Test
    public void getApproximateTime() throws Exception {
        Activity activity = new Activity();
        Date testDate = new Date();
        Field f = activity.getClass().getDeclaredField("approxTime");
        f.setAccessible(true);
        f.set(activity, testDate);
        assertThat(activity.getApproxTime())
                .isEqualTo(testDate);
    }
}
