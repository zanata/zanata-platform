/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.util;

import com.google.common.base.Optional;
import javaslang.control.Option;

/**
 * Converter functions for Option/Java Optional/Guava Optional
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@SuppressWarnings({ "Guava", "OptionalUsedAsFieldOrParameterType" })
public class Optionals {
    private Optionals() {
    }

    /**
     * @deprecated Handy for migration, but <code>option(option)</code> should be replaced with <code>option</code>.
     */
    @Deprecated
    public static <T> Option<T> option(Option<T> option) {
        return option;
    }

    public static <T> Option<T> option(Optional<T> optional) {
        return optional.isPresent() ? Option.of(optional.get()) : Option.none();
    }

    public static <T> Option<T> option(java.util.Optional<T> optional) {
        return optional.isPresent() ? Option.of(optional.get()) : Option.none();
    }

    public static <T> Optional<T> optionalGuava(Option<T> option) {
        return option.isDefined() ? Optional.of(option.get()) : Optional.absent();
    }

    public static <T> Optional<T> optionalGuava(java.util.Optional<T> option) {
        return option.isPresent() ? Optional.of(option.get()) : Optional.absent();
    }

    public static <T> java.util.Optional<T> optional(Option<T> option) {
        return option.isDefined() ? java.util.Optional.of(option.get()) : java.util.Optional.empty();
    }

    public static <T> java.util.Optional<T> optional(Optional<T> option) {
        return option.isPresent() ? java.util.Optional.of(option.get()) : java.util.Optional.empty();
    }

    /**
     * @deprecated Handy for migration, but <code>optional(optional)</code> should be replaced with <code>optional</code>.
     */
    @Deprecated
    public static <T> java.util.Optional<T> optional(
            java.util.Optional<T> option) {
        return option;
    }

}
