package org.zanata.rest.dto;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.junit.Test;
import org.zanata.common.TransUnitCount;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DTOSampleMarshallingTest {
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


    @Test
    public void testSampleDTORoundTrip() throws URISyntaxException,
            IOException {
        ContainerTranslationStatistics dto =
                new ContainerTranslationStatistics();
        dto.setId("sampleDoc");
        dto.addRef(new Link(new URI("http://localhost/a")));
        dto.addStats(new TranslationStatistics(
                new TransUnitCount(1, 2, 3, 4, 5), "de"));
        ContainerTranslationStatistics detailedStats =
                new ContainerTranslationStatistics();
        detailedStats.copyFrom(dto);
        dto.addDetailedStats(detailedStats);

        String jacksonMarshalledJson = jacksonMapper.writeValueAsString(dto);
        String mixedMarshalledJson = mixedMapper.writeValueAsString(dto);
        assertThat(jacksonMarshalledJson, equalTo(mixedMarshalledJson));
    }
}
