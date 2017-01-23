package org.zanata.rest;

import com.google.common.collect.ImmutableSet;
import org.atteo.classindex.ClassIndex;
import org.zanata.rest.service.RestResource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

@ApplicationPath("/rest")
@ApplicationScoped
public class JaxRSApplication extends javax.ws.rs.core.Application {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JaxRSApplication.class);

    private Set<Class<?>> classes = buildClassesSet();

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    /**
     * Collect all classes annotated with {@code @Path} or {@code @Provider},
     * except in the packages {@code org.zanata.rest.client} and
     * {@code org.zanata.rest.enunciate}.
     *
     * @return resource and provider classes
     */
    private static Set<Class<?>> buildClassesSet() {
        Iterable<Class<? extends RestResource>> resourceClasses =
                ClassIndex.getSubclasses(RestResource.class);
        log.debug("Indexed RestResource classes: {}", resourceClasses);
        Iterable<Class<?>> pathClasses = ClassIndex.getAnnotated(Path.class);
        log.debug("Indexed @Path classes: {}", pathClasses);
        Iterable<Class<?>> providerClasses =
                ClassIndex.getAnnotated(Provider.class);
        log.debug("Indexed @Provider classes: {}", providerClasses);
        ImmutableSet<Class<?>> classes = concat(stream(resourceClasses), concat(
                stream(pathClasses),
                stream(providerClasses))).filter(clazz -> !clazz.getName()
                        .startsWith("org.zanata.rest.client.")
                        && !clazz.getName()
                                .startsWith("org.zanata.rest.enunciate."))
                        .collect(Collectors.collectingAndThen(
                                Collectors.toSet(), ImmutableSet::copyOf));
        log.info("Found {} JAX-RS classes in total", classes.size());
        return classes;
    }

    private static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
