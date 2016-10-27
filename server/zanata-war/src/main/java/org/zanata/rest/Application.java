package org.zanata.rest;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.atteo.classindex.ClassIndex;

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
@Slf4j
public class Application extends javax.ws.rs.core.Application {

    private Set<Class<?>> classes = buildClassesSet();

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    /**
     * Collect all classes annotated with {@code @Path} or
     * {@code @Provider}, except in the packages
     * {@code org.zanata.rest.client} and {@code org.zanata.rest.enunciate}.
     * @return
     */
    private static Set<Class<?>> buildClassesSet() {
        Iterable<Class<?>> pathClasses = ClassIndex.getAnnotated(Path.class);
        log.debug("Indexed @Path classes: {}", pathClasses);
        assert pathClasses.iterator().hasNext();
        Iterable<Class<?>> providerClasses = ClassIndex.getAnnotated(Provider.class);
        log.debug("Indexed @Provider classes: {}", providerClasses);
        assert providerClasses.iterator().hasNext();
        return concat(
                stream(pathClasses),
                stream(providerClasses))
                .filter(clazz ->
                        !clazz.getName().startsWith("org.zanata.rest.client.") &&
                        !clazz.getName().startsWith("org.zanata.rest.enunciate."))
                .collect(Collectors.collectingAndThen(Collectors.toSet(),
                        ImmutableSet::copyOf));
    }

    private static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
