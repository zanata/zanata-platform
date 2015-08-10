package org.zanata.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.security.annotations.ZanataSecured;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * A sanity test to check whether annotations are set properly. e.g. all classes
 * that has @CheckLoggedIn, @CheckRole or @CheckPermission needs to have a class
 * level @ZanataSecured.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SecurityAnnotationTest {
    private static final Logger log =
            LoggerFactory.getLogger(SecurityAnnotationTest.class);
    private File srcBase;

    @Before
    public void setUp() {
        URL propUrl =
                Thread.currentThread().getContextClassLoader()
                        .getResource("testProp.properties");
        Preconditions.checkNotNull(propUrl);

        Properties properties = new Properties();
        try (InputStream in = propUrl.openStream()){
            properties.load(in);
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
        srcBase = new File(properties.getProperty("srcBase"));
    }

    @Test
    public void verifySecurityAnnotationsAreCorrectlySet() throws Exception {

        AnnotationDB annotationDB = new AnnotationDB();
        annotationDB.setScanPackages(new String[] { "org.zanata" });
        // has to have a '/' in the end otherwise it will use JarIterator rather
        // than DirectoryIteratorFactory
        annotationDB.scanArchives(new URI("file://" + srcBase.getAbsolutePath()
                + "/").toURL());

        Map<String, Set<String>> annotationIndex =
                annotationDB.getAnnotationIndex();
        Set<String> zanataSecured =
                annotationIndex.get(ZanataSecured.class.getName());

        Set<String> checkLoggedIn =
                annotationIndex.get(CheckLoggedIn.class.getName());

        // known exception (e.g. will be used by deltaspike in the future)
        checkLoggedIn.remove(
                CheckLoggedInProvider.class.getCanonicalName());

        Set<String> checkRole = annotationIndex.get(CheckRole.class.getName());

        Set<String> checkPermission =
                annotationIndex.get(CheckPermission.class.getName());

        Assertions.assertThat(zanataSecured)
                .containsAll(checkLoggedIn)
                .as("classes using @CheckLoggedIn all have @ZanataSecured");

        Assertions.assertThat(zanataSecured).containsAll(checkRole)
                .as("classes using @CheckRole all have @ZanataSecured");

        if (checkPermission != null) {
            Assertions.assertThat(zanataSecured).containsAll(checkPermission)
                    .as("classes using @CheckPermission all have @ZanataSecured");
        }

    }
}
