/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will check values passed in as option (commandline argument or
 * pom.xml) and defined in project config (zanata.xml).
 *
 * It will consider a value
 * presents only if it's not null; or if it's string, it's not blank; or if it's
 * a collection, it's not empty.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class OptionMismatchChecker<T> {
    private static final Logger log =
            LoggerFactory.getLogger(OptionMismatchChecker.class);
    private final T valueInOption;
    private final T valueInConfig;
    private final String fieldDesc;

    private OptionMismatchChecker(T valueInOption, T valueInConfig,
            String fieldDesc) {
        this.valueInOption = valueInOption;
        this.valueInConfig = valueInConfig;
        this.fieldDesc = fieldDesc;
    }

    public static <V> OptionMismatchChecker<V> from(V valueInOption,
            V valueInConfig, String fieldDesc) {
        return new OptionMismatchChecker<V>(valueInOption, valueInConfig,
                fieldDesc);
    }

    private boolean hasValueInOption() {
        return valueInOption != null && stringIsNotBlank(valueInOption)
                && collectionIsNotEmpty(valueInOption);
    }

    private boolean collectionIsNotEmpty(T value) {
        return !(value instanceof Collection) || !((Collection) value).isEmpty();
    }

    private boolean stringIsNotBlank(T value) {
        return !(value instanceof String) || !StringUtils.isBlank(
                (String) value);
    }

    private boolean hasValueInConfig() {
        return valueInConfig != null && stringIsNotBlank(valueInConfig)
                && collectionIsNotEmpty(valueInConfig);
    }

    private boolean hasValueInOptionOnly() {
        return hasValueInOption() && !hasValueInConfig();
    }

    public boolean hasValueInConfigOnly() {
        return !hasValueInOption() && hasValueInConfig();
    }

    private boolean hasValueInBoth() {
        return hasValueInOption() && hasValueInConfig();
    }

    private boolean valueMismatch() {
        return hasValueInBoth() && !valueInConfig.equals(valueInOption);
    }

    /**
     * If option is provided from commandline or in pom.xml but not in config
     * file, log a message to hint user that they can define it in config file.
     *
     * @param configExample
     *            how it should be defined in zanata.xml
     */
    public void logHintIfNotDefinedInConfig(String configExample) {
        if (hasValueInOptionOnly()) {
            log.info("You can define {} in zanata.xml now ({})", fieldDesc,
                    configExample);
        }
    }

    /**
     * If option values mismatch between project config and given value, log a
     * warning.
     */
    public void logWarningIfValuesMismatch() {
        if (valueMismatch()) {
            log.warn(
                    "{} in zanata.xml is set to [{}] but is now given as [{}]",
                    fieldDesc, valueInConfig, valueInOption);
            log.warn("You are overriding your {} defined in zanata.xml",
                    fieldDesc);
        }
    }
}
