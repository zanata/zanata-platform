/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.test;

import java.util.Arrays;
import java.util.List;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.parameterized.ParametersRunnerFactory;
import org.junit.runners.parameterized.TestWithParameters;

/**
 * Parameterized Runner for CDI-Unit. Based on
 * http://stackoverflow.com/a/27750897/14379
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class CdiUnitRunnerWithParameters extends CdiUnitRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(CdiUnitRunnerWithParameters.class);
    private final Object[] parameters;
    private final String paramsName;

    public CdiUnitRunnerWithParameters(TestWithParameters test)
            throws InitializationError {
        super(test.getTestClass().getJavaClass());
        this.parameters = test.getParameters().toArray();
        this.paramsName = test.getName();
    }

    private boolean fieldsAreAnnotated() {
        return !getParameterFields().isEmpty();
    }

    private List<FrameworkField> getParameterFields() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return method.getName() + paramsName;
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        // Unlike BlockJUnit4ClassRunnerWithParameters
        // we only support field injection for parameters
        // for compatibility with CDI constructor rules.
        if (!fieldsAreAnnotated()) {
            errors.add(new Exception(
                    "This runner only supports field injection for parameters"));
        }
        // TODO ParamTestCdiExtension might be added by AdditionalClasspaths or
        // AdditionalPackages
        AdditionalClasses additionalClasses =
                getTestClass().getAnnotation(AdditionalClasses.class);
        if (additionalClasses == null
                || !Arrays.asList(additionalClasses.value())
                        .contains(ParamTestCdiExtension.class)) {
            errors.add(new Exception(
                    "Please add @AdditionalClasses(ParamTestCdiExtension.class)"));
        }
    }

    @Override
    protected Object createTest() throws Exception {
        // TODO modify CdiRunner so that we can get the ParamTestCdiExtension
        // instance, and avoid the static setter method
        ParamTestCdiExtension.setCurrentTestParameters(parameters);
        try {
            return super.createTest();
        } finally {
            ParamTestCdiExtension.setCurrentTestParameters(null);
        }
    }

    public static class Factory implements ParametersRunnerFactory {

        @Override
        public Runner createRunnerForTestWithParameters(TestWithParameters test)
                throws InitializationError {
            return new CdiUnitRunnerWithParameters(test);
        }
    }
}
