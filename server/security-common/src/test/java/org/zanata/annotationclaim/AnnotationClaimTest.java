package org.zanata.annotationclaim;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("WeakerAccess")
public class AnnotationClaimTest {

    @Mock
    ProcessingEnvironment processingEnv;
    @Mock
    RoundEnvironment roundEnv;
    String runWith = "org.junit.runner.RunWith";
    String cacheable = "javax.persistence.Cacheable";
    String annotationsOpt = runWith + ",\n    " + cacheable;
    @Mock
    TypeElement runWithElem;
    @Mock
    Name runWithName;
    @Mock
    TypeElement nullableElem;
    @Mock
    Name nullableName;

    AnnotationClaim claim;
    Map<String, String> options;

    @Before
    public void setUp() {
        claim = new AnnotationClaim();
        options = new HashMap<>();
        when(processingEnv.getOptions()).thenReturn(options);

        when(runWithElem.getQualifiedName()).thenReturn(runWithName);
        when(runWithName.toString()).thenReturn(runWith);

        when(nullableElem.getQualifiedName()).thenReturn(nullableName);
        when(nullableName.toString()).thenReturn("javax.annotation.Nullable");
    }

    @Test
    public void missingOption() throws Exception {
        claim.init(processingEnv);
        assertThat(claim.getSupportedAnnotationTypes()).isEqualTo(emptySet());
    }

    @Test
    public void normalCase() throws Exception {
        options.put(AnnotationClaim.OPT_VERBOSE, "true");
        options.put(AnnotationClaim.OPT_ANNOTATIONS, annotationsOpt);

        claim.init(processingEnv);

        assertThat(claim.getSupportedSourceVersion()).isEqualTo(
                SourceVersion.latest());
        assertThat(claim.getSupportedAnnotationTypes()).isEqualTo(
                new HashSet<>(asList(runWith, cacheable)));

        assertThat(claim.process(new HashSet<>(asList(runWithElem, nullableElem)), roundEnv)).isFalse();
        assertThat(claim.process(singleton(runWithElem), roundEnv)).isTrue();

    }

}
