package org.zanata.rest;

import com.google.common.collect.ImmutableSet;
import org.jboss.resteasy.util.PickConstructor;
import org.reflections.Reflections;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

@ApplicationPath(JaxRSApplication.REST_APP_BASE)
@ApplicationScoped
public class JaxRSApplication extends javax.ws.rs.core.Application {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JaxRSApplication.class);

    private static final Set<Class<?>> classes = buildClassesSet();
    private static final String PACKAGE_PREFIX = "org.zanata";
    public static final String REST_APP_BASE = "/rest";

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
        Set<Class<?>> pathClasses =
                reflections.getTypesAnnotatedWith(Path.class);
        log.debug("Indexed @Path classes: {}", pathClasses);
        Set<Class<?>> providerClasses =
                reflections.getTypesAnnotatedWith(Provider.class);
        log.debug("Indexed @Provider classes: {}", providerClasses);
        Stream<Class<?>> concatStream = concat(
                pathClasses.stream(),
                providerClasses.stream());
        ImmutableSet<Class<?>> classes = concatStream
                // we don't want to pick up our JAX-RS client proxies
                .filter(clazz -> !clazz.getName()
                        .startsWith("org.zanata.rest.client."))
                .filter(JaxRSApplication::canConstruct)
                .collect(collectingAndThen(toSet(), ImmutableSet::copyOf));
        long timeTaken = currentTimeMillis() - start;
        log.info("Found {} JAX-RS classes in total; took {} ms", classes.size(),
                timeTaken);
        log.debug("JAX-RS classes: {}", classes);
        return classes;
    }

    private static boolean canConstruct(Class<?> clazz) {
        return !isAbstract(clazz.getModifiers()) &&
                // RESTEasy can use no-args constructor, or any constructor
                // with @Context args. This method should find either, but not
                // org.zanata.rest.service.raw.SourceAndTranslationResourceRestBase.TestSourceDocResource.
                PickConstructor.pickPerRequestConstructor(clazz) != null;
    }
}
