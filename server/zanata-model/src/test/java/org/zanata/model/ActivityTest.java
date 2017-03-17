package org.zanata.model;

import org.junit.Test;

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
}
