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

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class PermissionEvaluatorTest {

    private PermissionEvaluator simplePermissionEvaluator;
    private PermissionEvaluator complexPermissionEvaluator;

    @BeforeMethod
    public void initialize() {
        simplePermissionEvaluator = new PermissionEvaluator();
        complexPermissionEvaluator = new PermissionEvaluator();
        simplePermissionEvaluator.analyze(SimpleTestEvaluators.class);
        complexPermissionEvaluator.analyze(ComplexTestEvaluators.class);
    }

    @Test
    public void alwaysEvaluates() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesToTrueAlways");
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null)).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null,
                        new HProject())).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "my-action")).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "my-action", new HProject())).isTrue();
    }

    @Test
    public void evaluatesForClass() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesForHProject");

        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null, new HProject())).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "my-action", new HProject())).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null, new HProjectIteration())).isTrue();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "my-action", new HProjectIteration())).isTrue();
    }

    @Test
    public void evaluatesForAction() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesForAction");

        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null, new HProject())).isFalse();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "my-action", new HProject())).isFalse();
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        "specific-action", new HProject())).isTrue();
        // The evaluator method expects a target, so null won't be
        // evaluated
        assertThat(
                simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                        null, "specific-action")).isFalse();
    }

    @Test
    public void evaluatesForMultiTargets() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesForMultiTarget");

        assertThat(simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                "multi-target-action", new HProject(), new HLocale()
                )).isTrue();
    }

    @Test
    public void evaluatesForMultiTargetsInDifferentOrder() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesForMultiTarget");

        assertThat(simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                "multi-target-action", new HLocale(), new HProject()
                )).isTrue();
    }

    @Test
    public void evaluatesForMultiTargetsWithLessTargets() throws Exception {
        Method evaluator =
                getStaticMethod(SimpleTestEvaluators.class,
                        "evaluatesForMultiTarget");

        assertThat(simplePermissionEvaluator.evaluatorMethodApplies(evaluator,
                "multi-target-action", new HLocale()
                )).isTrue();
    }

    @Test
    public void alwaysGrant() throws Exception {
        assertThat(
                complexPermissionEvaluator.evaluatePermission("always-grant"))
                .isTrue();
    }

    @Test
    public void alwaysGrantWithSingleArgument() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("always-grant", new HProject())).isTrue();
    }

    @Test
    public void alwaysGrantWithMultipleArguments() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("always-grant", new HProject(), "A string",
                        new HProjectIteration())).isTrue();
    }

    @Test
    public void alwaysDenyForStringBuilder() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("any-action", new StringBuilder()))
                .isFalse();
    }

    @Test
    public void allowSpecificActionAndType() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("action-1", new HProject())).isTrue();
    }

    @Test
    public void allowCorrectActionButDifferentType() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("action-1", new HProjectIteration()))
                .isTrue();
    }

    @Test
    public void allowMatchingActionWithMultipleTargets() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("action-1", new HProjectIteration(),
                        "A string")).isTrue();
    }

    @Test
    public void allowAnyMultiAction() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-1", new HProjectIteration()))
                .isTrue();
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-2", new HProjectIteration()))
                .isTrue();
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-3", new HProjectIteration()))
                .isTrue();
    }

    @Test
    public void allowCorrectMultiActionWithDifferentType() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-1", new HProject()))
                .isTrue();
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-2", new HProject()))
                .isTrue();
        assertThat(complexPermissionEvaluator
                .evaluatePermission("multi-action-3", new HProject()))
                .isTrue();
    }

    @Test
    public void injectActionAndAllow() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("action-should-grant", new HAccount()))
                .isTrue();
    }

    @Test
    public void injectActionAndDeny() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("action-should-deny", new HAccount()))
                .isFalse();
    }

    @Test
    public void multiTargetAllow() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("please-allow", new LocaleId("de-DE"),
                        new HProject())).isTrue();
    }

    @Test
    public void multiTargetDenyWithWrongAction() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("please-deny", new LocaleId("de-DE"),
                        new HProject())).isFalse();
    }

    @Test
    public void multiTargetDenyWithDifferentTargetTypes() throws Exception {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("please-allow", new LocaleId("de-DE"),
                        new HProjectIteration())).isFalse();
    }

    @Test
    public void multiTargetDenyCheckFailed() throws Exception {
        // Only allows German
        assertThat(complexPermissionEvaluator
                .evaluatePermission("please-allow", new LocaleId("es-ES"),
                        new HProject())).isFalse();
    }

    @Test
    public void multiTargetAllowWithTargetsInDifferentOrder() {
        assertThat(complexPermissionEvaluator
                .evaluatePermission("please-allow", new HProject(),
                        new LocaleId("de-DE"))).isTrue();
    }

    @Test
    public void allowWhenAllArgsPresentInOrder() {
        assertThat(
                complexPermissionEvaluator
                        .evaluatePermission("only-when-all-args-present",
                                new HProject(),
                                new HProjectIteration(), new HLocale()))
                .isTrue();
    }

    @Test
    public void allowWhenAllArgsPresentInDisorder() {
        assertThat(
                complexPermissionEvaluator
                        .evaluatePermission("only-when-all-args-present",
                                new HLocale(),
                                new HProjectIteration(), new HProject()))
                .isTrue();
    }

    @Test
    public void denyWhenSomeArgsNorPresentAndAreRequired() {
        assertThat(
                complexPermissionEvaluator
                        .evaluatePermission("only-when-all-args-present",
                                new HProjectIteration()))
                .isFalse();
    }

    @Test
    public void denyWhenException() {
        Method evaluator =
                getStaticMethod(ComplexTestEvaluators.class,
                        "deniesWithException");
        assertThat(complexPermissionEvaluator.evaluatorMethodApplies(evaluator,
                "throws-exception", new HProjectIteration()));
        assertThat(
                complexPermissionEvaluator
                        .evaluatePermission("throws-exception",
                                new HProjectIteration()))
                .isFalse();
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

        @ResolvesPermissions
        public static boolean evaluatesToTrueAlways() {
            return true;
        }

        @ResolvesPermissions
        public static boolean evaluatesForHProject(HProject project) {
            return true;
        }

        @ResolvesPermissions(action = "specific-action")
        public static boolean evaluatesForAction(Object target,
                @Action String action) {
            return true;
        }

        @ResolvesPermissions(action = "multi-target-action")
        public static boolean evaluatesForMultiTarget(HProject project,
                HLocale locale, @Action String action) {
            return project != null && locale != null;
        }
    }

    public static class ComplexTestEvaluators {

        @ResolvesPermissions(action = "always-grant")
        public static boolean alwaysGrant() {
            return true;
        }

        // Do not define any other methods that take a StringBuilder
        @ResolvesPermissions
        public static boolean alwaysDenyForStringBuilders(StringBuilder target) {
            return false;
        }

        @ResolvesPermissions(action = "action-1")
        public static boolean allowAction1ForProjects(HProject project) {
            return true;
        }

        @ResolvesPermissions(action = { "multi-action-1", "multi-action-2",
                "multi-action-3" })
        public static boolean allowMultipleActionsForProjectIterations(
                HProjectIteration iteration) {
            return true;
        }

        // Do not define any other methods that take an HAccount
        @ResolvesPermissions
        public static boolean programaticallyAllowForAction(HAccount account,
                @Action String action) {
            return action.endsWith("should-grant");
        }

        // Multi target method
        @ResolvesPermissions(action = "please-allow")
        public static boolean allowForGermanAndAnyProject(LocaleId locale,
                HProject project, @Action String action) {
            return locale.getId().startsWith("de")
                    && action.equals("please-allow") && project != null;
        }

        @ResolvesPermissions(action = "only-when-all-args-present")
        public static boolean allowsWhenAllArgsPresent(HProject project,
                HProjectIteration iteration, HLocale locale) {
            return project != null && iteration != null && locale != null;
        }

        // Exception method
        @ResolvesPermissions(action = "throws-exception")
        public static boolean deniesWithException() {
            throw new RuntimeException("permission denied");
        }
    }
}
