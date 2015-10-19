package org.zanata.util;

import org.atteo.classindex.processor.ClassIndexProcessor;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

/**
 * Tells ClassIndex to index all classes with the annotation {@code @Path} or
 * {@code @Provider}.
 * <p>
 * This class should be activated by putting
 * {@code META-INF/services/javax.annotation.processing.Processor} on the
 * classpath when compiling zanata-war.
 * </p>
 */
public class JaxRSClassIndexProcessor extends ClassIndexProcessor {
    public JaxRSClassIndexProcessor() {
        indexAnnotations(Path.class);
        indexAnnotations(Provider.class);
    }
}
