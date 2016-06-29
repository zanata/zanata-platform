/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.config;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When you want to produce a system configuration value, you can use this
 * qualifier with a unique {@link SysConfig#value()} across the system. At the
 * injection point, using the same binding in {@link SysConfig#value()} so
 * that you will get the produced configuration value.
 * <p/>
 * Example:
 * <pre>
 *     {@code @Produces @SysConfig("supportOAuth")
 *         boolean getSupportOAuthConfig() {
 *             // note the key can be different from what's in the binding.
 *             // ideally we may want to use the same value in binding
 *             return Boolean.parseBoolean(System.getProperties("support.oauth"));
 *         }
 *     }
 * </pre>
 * Then you can inject it by:
 * <pre>
 *     {@code @Inject @SysConfig("supportOAuth") boolean isOAuthSupported;}
 * </pre>
 *
 */
@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface SysConfig {
    /**
     * @return The name of the config
     */
    String value();

}
