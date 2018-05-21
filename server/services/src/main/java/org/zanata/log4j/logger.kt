package org.zanata.log4j

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */

/**
 * Returns a logger for the package containing the lambda (which may be empty: `getLogger {}`)
 */
fun getLogger(lambda: () -> Any): Logger =
        getLoggerForPackage(lambda.javaClass)

private fun getLoggerForPackage(clazz: Class<*>): Logger =
        LoggerFactory.getLogger(getPackage(clazz))

private fun getPackage(clazz: Class<*>): String {
    val className = clazz.name
    val lastDot = className.lastIndexOf('.')
    return if (lastDot >= 0)
        className.substring(0, lastDot)
    else
        ""
}

