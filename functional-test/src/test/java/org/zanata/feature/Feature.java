package org.zanata.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public @interface Feature {
    int NO_BUG = 0;

    int bugzilla() default NO_BUG;
    String summary() default "no description";
    int[] tcmsTestPlanIds() default {};
    int[] tcmsTestCaseIds() default {};
}
