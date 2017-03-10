/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PropertiesHolder {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PropertiesHolder.class);

    public static final Properties properties;
    static {
        Properties result;
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(Constants.propFile.value());
        if (inputStream == null) {
            throw new RuntimeException("can\'t find setup.properties");
        }
        Properties properties1 = new Properties();
        try {
            properties1.load(inputStream);
            result = properties1;
        } catch (IOException e) {
            PropertiesHolder.log.error("can\'t load {}", Constants.propFile);
            throw new IllegalStateException("can\'t load setup.properties");
        }
        properties = result;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
