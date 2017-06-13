/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.annotationclaim;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@SupportedOptions({ AnnotationClaim.OPT_ANNOTATIONS })
public class AnnotationClaim extends AbstractProcessor {
    static final String OPT_ANNOTATIONS =
            "org.zanata.annotationclaim.annotations";
    static final String OPT_VERBOSE =
            "org.zanata.annotationclaim.verbose";
    private Set<String> annotationsToClaim;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return annotationsToClaim;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        Map<String, String> options = processingEnv.getOptions();
        String annoOption = options.get(OPT_ANNOTATIONS);
        boolean verbose = parseBoolean(options.get(OPT_VERBOSE));
        if (verbose) {
            System.out.println("AnnotationClaimer is active.");
        }
        super.init(processingEnv);
        Set<String> annotationsToClaim = new HashSet<>();
        if (annoOption != null) {
            String[] annotations = annoOption.trim().split("[,\\s]+");
            addAll(annotationsToClaim, annotations);
            if (verbose) {
                System.out.println("AnnotationClaimer claiming annotations:");
                annotationsToClaim.forEach(it -> System.out.println("  " + it));
            }
        } else {
            System.out.println("AnnotationClaimer not claiming annotations: " +
                    OPT_ANNOTATIONS + " is not set.");
        }
        this.annotationsToClaim = unmodifiableSet(annotationsToClaim);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        Set<String> names =
                annotations.stream().map(it -> it.getQualifiedName().toString())
                        .collect(toSet());
        return annotationsToClaim.containsAll(names);
    }
}
