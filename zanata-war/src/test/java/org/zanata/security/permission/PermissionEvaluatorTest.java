/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.security.permission;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.security.SecurityFunctions;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class PermissionEvaluatorTest {

    private final SoftAssertions softly = new SoftAssertions();
    private PermissionEvaluator simplePermissionEvaluator;
    private PermissionEvaluator complexPermissionEvaluator;

    @Before
    public void initialize() {
        simplePermissionEvaluator = new PermissionEvaluator();
        complexPermissionEvaluator = new PermissionEvaluator();
        simplePermissionEvaluator.registerPermissionGranters(
                SimpleTestEvaluators.class);
        complexPermissionEvaluator.registerPermissionGranters(
                ComplexTestEvaluators.class);
    }

    @Test
    public void testGranterRegistration() throws Exception {
        new PermissionEvaluator().registerPermissionGranters(
                SecurityFunctions.class);
    }

    @Test
    public void alwaysEvaluates() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(getStaticMethod(
                        SimpleTestEvaluators.class,
                        "evaluatesToTrueAlways"));

        softly.assertThat(
                granter.shouldInvokeGranter(null)).isTrue();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        null,
                        new HProject())).isTrue();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        "my-action")).isTrue();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        "my-action", new HProject())).isTrue();
        softly.assertAll();
    }

    @Test
    public void evaluatesForClass() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(
                        getStaticMethod(SimpleTestEvaluators.class,
                                "evaluatesForHProject"));

        softly.assertThat(
                granter.shouldInvokeGranter(
                        null, new HProject())).isTrue();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        "my-action", new HProject())).isTrue();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        null, new HProjectIteration())).isFalse();
        softly.assertThat(
                granter.shouldInvokeGranter(
                        "my-action", new HProjectIteration())).isFalse();
        softly.assertAll();
    }

    @Test
    public void evaluatesForMultiTargets() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(getStaticMethod(
                        SimpleTestEvaluators.class,
                        "evaluatesForMultiTarget"));

        assertThat(granter.shouldInvokeGranter(
                "multi-target-action", new HProject(), new HLocale()
        )).isTrue();
    }

    @Test
    public void evaluatesForMultiTargetsInDifferentOrder() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(getStaticMethod(
                        SimpleTestEvaluators.class,
                        "evaluatesForMultiTarget"));

        assertThat(granter.shouldInvokeGranter(
                "multi-target-action", new HLocale(), new HProject()
        )).isTrue();
    }

    @Test
    public void doesNotEvaluateForLessTargetsThanExpected() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(
                        getStaticMethod(SimpleTestEvaluators.class,
                                "evaluatesForMultiTarget"));

        assertThat(granter.shouldInvokeGranter(
                "multi-target-action", new HLocale()
        )).isFalse();
    }

    @Test
    public void evaluatesForMoreTargetsThanExpected() throws Exception {
        PermissionGranter granter =
                new PermissionGranter(
                        getStaticMethod(SimpleTestEvaluators.class,
                                "evaluatesForMultiTarget"));

        assertThat(
                granter.shouldInvokeGranter(
                        "multi-target-action", new HLocale(), new HProject(),
                        new HProjectIteration(), new StringBuilder()
                        )).isTrue();
    }

    @Test
    public void alwaysGrant() throws Exception {
        assertThat(
                complexPermissionEvaluator.checkPermission("always-grant"))
                .isTrue();
    }

    @Test
    public void alwaysGrantWithSingleArgument() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("always-grant", new HProject())).isTrue();
    }

    @Test
    public void alwaysGrantWithMultipleArguments() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("always-grant", new HProject(), "A string",
                        new HProjectIteration())).isTrue();
    }

    @Test
    public void alwaysDenyForStringBuilder() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("any-action", new StringBuilder()))
                .isFalse();
    }

    @Test
    public void allowSpecificActionAndType() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("project-action", new HProject())).isTrue();
    }

    @Test
    public void denyCorrectActionButDifferentType() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("project-action", new HProjectIteration()))
                .isFalse();
    }

    @Test
    public void allowMatchingActionWithMultipleTargets() throws Exception {
        // As long as all the granter parameters are supplied, the rest of
        // arguments can be ignored
        assertThat(complexPermissionEvaluator
                .checkPermission("project-action", new HProject(),
                        "A string")).isTrue();
    }

    @Test
    public void allowAnyMultiAction() throws Exception {
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-1", new HProjectIteration()))
                .isTrue();
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-2", new HProjectIteration()))
                .isTrue();
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-3", new HProjectIteration()))
                .isTrue();
        softly.assertAll();
    }

    @Test
    public void denyCorrectMultiActionWithDifferentType() throws Exception {
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-1", new HProject()))
                .isFalse();
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-2", new HProject()))
                .isFalse();
        softly.assertThat(complexPermissionEvaluator
                .checkPermission("multi-action-3", new HProject()))
                .isFalse();
        softly.assertAll();
    }

    @Test
    public void injectActionAndAllow() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("action-should-grant", new HAccount()))
                .isTrue();
    }

    @Test
    public void injectActionAndDeny() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("action-should-deny", new HAccount()))
                .isFalse();
    }

    @Test
    public void multiTargetAllow() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("please-allow", new LocaleId("de-DE"),
                        new HProject())).isTrue();
    }

    @Test
    public void multiTargetDenyWithWrongAction() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("please-deny", new LocaleId("de-DE"),
                        new HProject())).isFalse();
    }

    @Test
    public void multiTargetDenyWithDifferentTargetTypes() throws Exception {
        assertThat(complexPermissionEvaluator
                .checkPermission("please-allow", new LocaleId("de-DE"),
                        new HProjectIteration())).isFalse();
    }

    @Test
    public void multiTargetDenyCheckFailed() throws Exception {
        // Only allows German
        assertThat(complexPermissionEvaluator
                .checkPermission("please-allow", new LocaleId("es-ES"),
                        new HProject())).isFalse();
    }

    @Test
    public void multiTargetAllowWithTargetsInDifferentOrder() {
        assertThat(complexPermissionEvaluator
                .checkPermission("please-allow", new HProject(),
                        new LocaleId("de-DE"))).isTrue();
    }

    @Test
    public void allowWhenAllArgsPresentInOrder() {
        assertThat(
                complexPermissionEvaluator
                        .checkPermission("only-when-all-args-present",
                                new HProject(),
                                new HProjectIteration(), new HLocale()))
                .isTrue();
    }

    @Test
    public void allowWhenAllArgsPresentInDisorder() {
        assertThat(
                complexPermissionEvaluator
                        .checkPermission("only-when-all-args-present",
                                new HLocale(),
                                new HProjectIteration(), new HProject()))
                .isTrue();
    }

    @Test
    public void denyWhenSomeRequiredArgsAbsent() {
        assertThat(
                complexPermissionEvaluator
                        .checkPermission("only-when-all-args-present",
                                new HProjectIteration()))
                .isFalse();
    }

    @Test
    public void exceptionInGranterIsPropagated() {
        try {
            assertThat(
                    complexPermissionEvaluator
                            .checkPermission("throws-exception",
                                    new HProjectIteration()))
                    .isFalse();
        } catch (Exception e) {
            assertThat(e).isExactlyInstanceOf(RuntimeException.class);
            assertThat(e).hasCauseInstanceOf(InvocationTargetException.class);
        }
    }

    private Method getStaticMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    public static class SimpleTestEvaluators {

        @GrantsPermission
        public static boolean evaluatesToTrueAlways() {
            return true;
        }

        @GrantsPermission
        public static boolean evaluatesForHProject(HProject project) {
            return true;
        }

        @GrantsPermission(actions = "specific-action")
        public static boolean evaluatesForAction(Object target,
                @Action String action) {
            return true;
        }

        @GrantsPermission(actions = "multi-target-action")
        public static boolean evaluatesForMultiTarget(HProject project,
                HLocale locale, @Action String action) {
            return project != null && locale != null;
        }
    }

    public static class ComplexTestEvaluators {

        @GrantsPermission(actions = "always-grant")
        public static boolean alwaysGrant() {
            return true;
        }

        // Do not define any other methods that take a StringBuilder
        @GrantsPermission
        public static boolean alwaysDenyForStringBuilders(StringBuilder target) {
            return false;
        }

        @GrantsPermission(actions = "project-action")
        public static boolean allowAction1ForProjects(HProject project) {
            return true;
        }

        @GrantsPermission(actions = { "multi-action-1", "multi-action-2",
                "multi-action-3" })
        public static boolean allowMultipleActionsForProjectIterations(
                HProjectIteration iteration) {
            return true;
        }

        // Do not define any other methods that take an HAccount
        @GrantsPermission
        public static boolean programaticallyAllowForAction(HAccount account,
                @Action String action) {
            return action.endsWith("should-grant");
        }

        // Multi target method
        @GrantsPermission(actions = "please-allow")
        public static boolean allowForGermanAndAnyProject(LocaleId locale,
                HProject project, @Action String action) {
            return locale.getId().startsWith("de")
                    && action.equals("please-allow") && project != null;
        }

        @GrantsPermission(actions = "only-when-all-args-present")
        public static boolean allowsWhenAllArgsPresent(HProject project,
                HProjectIteration iteration, HLocale locale) {
            return project != null && iteration != null && locale != null;
        }

        // Exception method
        @GrantsPermission(actions = "throws-exception")
        public static boolean deniesWithException() {
            throw new RuntimeException("permission denied");
        }
    }
}
