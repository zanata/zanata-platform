package org.zanata.rest.service;

import java.util.Arrays;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.dao.LocaleDAO;
import org.zanata.test.CdiUnitRunnerWithParameters;
import org.zanata.test.ParamTestCdiExtension;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@UseParametersRunnerFactory(CdiUnitRunnerWithParameters.Factory.class)
@InRequestScope
@AdditionalClasses(ParamTestCdiExtension.class)
public class ResourceUtilsParamTest extends ZanataTest {

    @Inject
    private ResourceUtils resourceUtils;

    @Produces
    @Mock
    private EntityManager mockEm;

    @Produces
    @Mock
    private LocaleDAO mockLocaleDAO;

    @Parameter(0)
    String encoded;
    @Parameter(1)
    String decoded;

    @Parameterized.Parameters(name = "{index}: enc({0})<->dec({1})")
    public static Iterable<Object[]> urlPatterns() {
        return Arrays.asList(new Object[][] {
                { ",my,doc,id", "/my/doc/id" },
                { ",my,,doc,id", "/my//doc/id" },
                { "x+y", "x y" }
        });
    }

    @Test
    public void decodeDocIds() {
        assertThat(resourceUtils.decodeDocId(encoded))
                .as("Decoding " + encoded)
                .isEqualTo(decoded);
    }

    @Test
    public void encodeDocIds() {
        assertThat(resourceUtils.encodeDocId(decoded))
                .as("Encoding " + decoded)
                .isEqualTo(encoded);
    }
}
