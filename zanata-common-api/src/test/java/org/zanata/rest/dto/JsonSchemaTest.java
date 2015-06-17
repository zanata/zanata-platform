package org.zanata.rest.dto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.schema.JsonSchema;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.TransUnitCount;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.contribution.ContributionStatistics;
import org.zanata.rest.dto.stats.contribution.LocaleStatistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Parameterized.class)
public class JsonSchemaTest {
    private static final Logger log =
            LoggerFactory.getLogger(JsonSchemaTest.class);

    private JaxbAnnotationIntrospector jaxbAnnotationIntrospector =
            new JaxbAnnotationIntrospector();
    private JacksonAnnotationIntrospector jacksonAnnotationIntrospector =
            new JacksonAnnotationIntrospector();
    private final ObjectMapper jacksonMapper = new ObjectMapper();
    private final ObjectMapper mixedMapper = new ObjectMapper();

    /**
     * this one will use jackson annotation only (as in RESTEasy 2)
     */
    private final SerializationConfig jacksonConfig = jacksonMapper
            .getSerializationConfig()
            .withAnnotationIntrospector(
                    jacksonAnnotationIntrospector);

    /**
     * this one will use both jackson and jaxb (lower priority) annotation (as
     * in RESTEasy 3)
     */
    private final SerializationConfig mixedConfig = mixedMapper
            .getSerializationConfig()
            .withAnnotationIntrospector(
                    jacksonAnnotationIntrospector)
            .withAppendedAnnotationIntrospector(
                    jaxbAnnotationIntrospector);

    @Parameterized.Parameters(name = "{index}: DTO ({0})")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { SimpleComment.class },
                { HeaderEntry.class },
                { PoHeader.class },
                { PoTargetHeader.class },
                { PotEntryHeader.class },
                { Resource.class },
                { ResourceMeta.class },
                { TextFlow.class },
                { TextFlowTarget.class },
                { TranslationsResource.class },
                { ContributionStatistics.class },
                { LocaleStatistics.class },
                // this one cuases stack overflow in schema generation. Will
                // cover it in DTOSampleMarshallingTest
                // { ContainerTranslationStatistics.class },
                { TranslationStatistics.class },
                { Account.class },
                { CopyTransStatus.class },
                { Glossary.class },
                { GlossaryEntry.class },
                { GlossaryTerm.class },
                { LocaleDetails.class },
                { Person.class },
                { ProcessStatus.class },
                { Project.class },
                { ProjectIteration.class },
                { VersionInfo.class }
        });
    }

    private Class dtoClass;

    public JsonSchemaTest(Class input) {
        dtoClass = input;
    }

    @Test
    public void jsonSchemaIsTheSameWithAndWithoutJaxbAnnotation()
            throws IOException {
        log.debug("checking json schema for: {}", dtoClass);
        JsonSchema jsonSchemaWithJackson =
                jacksonMapper.generateJsonSchema(dtoClass, jacksonConfig);

        JsonSchema jsonSchemaWithJaxb = mixedMapper.generateJsonSchema(
                dtoClass,
                mixedConfig);

        log.debug("jackson only schema: {}", jsonSchemaWithJackson);
        log.debug("jackson and jaxb mixed schema: {}", jsonSchemaWithJaxb);
        assertThat(jsonSchemaWithJackson.toString(),
                equalTo(jsonSchemaWithJaxb.toString()));
    }
}
