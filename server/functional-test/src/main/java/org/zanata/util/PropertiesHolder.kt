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
package org.zanata.util

import java.io.IOException
import java.util.Properties

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
object PropertiesHolder {
    private val log = org.slf4j.LoggerFactory.getLogger(PropertiesHolder::class.java)

    val properties: Properties

    init {
        val result: Properties
        val inputStream = Thread.currentThread().contextClassLoader
                .getResourceAsStream(Constants.propFile.value())
                ?: throw RuntimeException("can\'t find setup.properties")
        val properties1 = Properties()
        try {
            properties1.load(inputStream)
            result = properties1
        } catch (e: IOException) {
            PropertiesHolder.log.error("can\'t load {}", Constants.propFile)
            throw IllegalStateException("can\'t load setup.properties")
        }

        properties = result
    }

    fun getProperty(key: String): String {
        return properties.getProperty(key)
    }

    fun getProperty(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }
}
