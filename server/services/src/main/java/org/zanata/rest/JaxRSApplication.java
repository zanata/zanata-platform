package org.zanata.rest;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.zanata.rest.service.RestResource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

@ApplicationPath("/rest")
@ApplicationScoped
public class JaxRSApplication extends javax.ws.rs.core.Application {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JaxRSApplication.class);

    private static final Set<Class<?>> classes = buildClassesSet();
    private static final String PACKAGE_PREFIX = "org.zanata";

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    /**
     * Collect all non-abstract Zanata classes annotated with {@code @Path} or
     * {@code @Provider}, except in the package {@code org.zanata.rest.client}.
     *
     * @return resource and provider classes
     */
    private static Set<Class<?>> buildClassesSet() {
        long start = currentTimeMillis();
        Reflections reflections = new Reflections(PACKAGE_PREFIX);
        Iterable<Class<? extends RestResource>> resourceClasses =
                reflections.getSubTypesOf(RestResource.class);
        log.debug("Indexed RestResource classes: {}", resourceClasses);
        Iterable<Class<?>> pathClasses = reflections.getTypesAnnotatedWith(Path.class, true);
        log.debug("Indexed @Path classes: {}", pathClasses);
        Iterable<Class<?>> providerClasses =
                reflections.getTypesAnnotatedWith(Provider.class, true);
        log.debug("Indexed @Provider classes: {}", providerClasses);
        Stream<Class<?>> concatStream = concat(stream(resourceClasses), concat(
                stream(pathClasses),
                stream(providerClasses)));
        ImmutableSet<Class<?>> classes = concatStream
                .filter(clazz -> !isAbstract(clazz.getModifiers()))
                .filter(clazz -> !clazz.getName()
                        .startsWith("org.zanata.rest.client."))
                .collect(collectingAndThen(toSet(), ImmutableSet::copyOf));
        long timeTaken = currentTimeMillis() - start;
        log.info("Found {} JAX-RS classes in total; took {} ms", classes.size(),
                timeTaken);
        return classes;
    }

    private static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
