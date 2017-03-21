package org.zanata.model;

import org.junit.Test;
import org.zanata.common.ActivityType;
import org.zanata.common.ProjectType;

import java.lang.reflect.Field;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;

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

        // Test actor
        assertEqualsFalse(activity, new Activity(actorB, versionA, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000));
        // Test context
        assertEqualsFalse(activity, new Activity(actorA, document, versionA,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 9000));
        // Test activity type
        assertEqualsFalse(activity, new Activity(actorA, versionA, versionA,
                ActivityType.REVIEWED_TRANSLATION, 9000));
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
        // Only change business key items
        hashCode2 = new Activity(actorA, versionA, versionB,
                ActivityType.UPLOAD_SOURCE_DOCUMENT, 1000).hashCode();
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
