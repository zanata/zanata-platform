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
    public void getApproximateTime() throws Exception {
        Activity activity = new Activity();
        Date testDate = new Date();
        System.out.println(testDate.toString());
        Field f = activity.getClass().getDeclaredField("approxTime");
        f.setAccessible(true);
        f.set(activity, testDate);
        assertThat(activity.getApproxTime())
                .isEqualTo(testDate);
    }

    @Test
    public void testEquals() {
        Date now = new Date();

        HDocument document = new HDocument();
        document.setId(555666777L);
        HPerson actorA = new HPerson();
        actorA.setName("lara");
        actorA.setEmail("lara@test.com");
        actorA.setId(321654987L);
        actorA.setLastChanged(now);
        actorA.setCreationDate(now);

        HPerson actorB = new HPerson();
        actorB.setId(987654321L);
        actorB.setName("aloy");
        actorB.setEmail("aloy@test.com");
        actorB.setLastChanged(now);
        actorB.setCreationDate(now);

        HProjectIteration versionA = new HProjectIteration();
        versionA.setId(1234567890L);
        HProjectIteration versionB = new HProjectIteration();
        versionB.setId(5678901234L);
        versionB.setProjectType(ProjectType.Utf8Properties);

        Activity activity = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        activity.setCreationDate(now);
        activity.setLastChanged(now);

        Activity other = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        other.setCreationDate(now);
        other.setLastChanged(now);

        assertThat(activity.equals(other)).isTrue();

        // Test actor
        other = new Activity(actorB, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertEqualsFalse(activity, other);
        // Test context
        other = new Activity(actorA, document, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertEqualsFalse(activity, other);
        // Test activity type
        other = new Activity(actorA, versionA, versionA,
                ActivityType.REVIEWED_TRANSLATION, 9000);
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertEqualsFalse(activity, other);
    }

    private void assertEqualsFalse(Activity a, Activity b) {
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    public void hashcodeTest() {
        Date now = new Date();
        HDocument document = new HDocument();
        document.setId(555666777L);
        HPerson actorA = new HPerson();
        actorA.setCreationDate(now);
        actorA.setLastChanged(now);
        HPerson actorB = new HPerson();
        actorB.setCreationDate(now);
        actorB.setLastChanged(now);
        actorB.setName("aloy");

        HProjectIteration versionA = new HProjectIteration();
        versionA.setId(1234567890L);
        HProjectIteration versionB = new HProjectIteration();
        versionB.setId(5678901234L);
        versionB.setProjectType(ProjectType.Utf8Properties);

        Activity activity = new Activity(actorA, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000);
        activity.setCreationDate(now);
        activity.setLastChanged(now);
        int hashCode1 = activity.hashCode();
        Activity other = new Activity(actorB, versionB, versionB,
                ActivityType.REVIEWED_TRANSLATION, 1000);
        other.setCreationDate(now);
        other.setLastChanged(now);
        int hashCode2 = other.hashCode();
        assertThat(hashCode1 == hashCode2).isFalse();
        // Only change business key items
        other = new Activity(actorA, versionA, versionB,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 1000);
        other.setCreationDate(now);
        other.setLastChanged(now);
        hashCode2 = other.hashCode();
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

}
