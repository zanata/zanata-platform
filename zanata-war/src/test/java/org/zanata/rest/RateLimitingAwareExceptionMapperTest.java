package org.zanata.rest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hamcrest.Matchers;
import org.scannotation.AnnotationDB;
import org.testng.annotations.Test;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class RateLimitingAwareExceptionMapperTest {

    @Test
    public void scanRestEasyExceptionMapper() throws IOException {
        String classFilePath =
                getClassFilePathOnClasspath(RateLimitingAwareExceptionMapper.class);
        URL uri = getPackagePathAsUrl(classFilePath);
        log.debug("url: {}", uri);

        AnnotationDB db = new AnnotationDB();
        db.scanArchives(uri);
        Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();

        Set<String> providers = annotationIndex.get(Provider.class.getName());
        log.debug("provider classes under this url: {}", providers);
        Iterable<String> exceptionMappersNotExtendingBaseMapper =
                Iterables.filter(providers, new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        Class<?> clazz = loadAsClass(input);
                        return isExceptionMapper(clazz)
                                && notExtendingBaseRateLimitingMapper(clazz);
                    }
                });

        // ensure REST exception mapper all extends
        // RateLimitingAwareExceptionMapper
        assertThat(exceptionMappersNotExtendingBaseMapper,
                Matchers.<String> emptyIterable());
    }

    private static String getClassFilePathOnClasspath(Class<?> classToUse) {
        String packagePath =
                classToUse.getPackage().getName().replaceAll("\\.", "/");

        String className = classToUse.getSimpleName() + ".class";

        return packagePath + "/" + className;
    }

    private static URL getPackagePathAsUrl(String classFilePath)
            throws MalformedURLException {
        URL url =
                Thread.currentThread().getContextClassLoader()
                        .getResource(classFilePath);
        return new File(url.getPath()).getParentFile().toURI().toURL();
    }

    private static Class<?> loadAsClass(String input) {
        Class<?> clazz;
        try {
            clazz = Class.forName(input);
        } catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
        return clazz;
    }

    private static boolean isExceptionMapper(Class<?> clazz) {
        return ExceptionMapper.class.isAssignableFrom(clazz);
    }

    private static boolean notExtendingBaseRateLimitingMapper(Class<?> clazz) {
        return !RateLimitingAwareExceptionMapper.class.isAssignableFrom(clazz);
    }
}
