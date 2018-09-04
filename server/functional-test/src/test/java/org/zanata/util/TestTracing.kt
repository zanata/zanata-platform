/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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

import java.io.File
import java.io.IOException

import org.codehaus.jackson.map.ObjectMapper
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import com.google.common.base.Optional
import com.google.common.collect.Lists

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TestTracing : RunListener() {

    private var fileReport: Boolean = false
    private val currentSpecification = ThreadLocal<Trace>()
    private var report: File? = null
    private val objectMapper = ObjectMapper()
    private val entries = Lists.newArrayList<TraceEntry>()

    @Throws(Exception::class)
    override fun testRunStarted(description: Description?) {
        super.testRunStarted(description)
        val locationPath = System.getProperty("traceLocation",
                "./target/test-classes/traceability")
        val location = File(locationPath)
        report = File(location, "traceability.json")
        if (!location.exists() && !location.mkdirs()) {
            throw RuntimeException("Unable to create traceability report dir")
        }
        if (report!!.exists() && !report!!.delete()) {
            throw RuntimeException("Unable to remove traceability report")
        }
        fileReport = report!!.createNewFile()
    }

    @Throws(Exception::class)
    override fun testRunFinished(result: Result?) {
        super.testRunFinished(result)
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(report, entries)
    }

    @Throws(Exception::class)
    override fun testStarted(description: Description?) {
        super.testStarted(description)
        val specOptional = getSpecification(description!!)
        if (specOptional.isPresent) {
            currentSpecification.set(specOptional.get())
        }
    }

    /**
     * annotation on method takes precedence over annotation on class. At the
     * moment class level annotation will cause multiple entries in the result
     * if there are more than one test methods.
     */
    private fun getSpecification(description: Description): Optional<Trace> {
        val testClassTrace = description.testClass.getAnnotation(Trace::class.java)
        val testMethodTrace = description.getAnnotation(Trace::class.java)
        return Optional.fromNullable(testMethodTrace).or(
                Optional.fromNullable(testClassTrace))
    }

    @Throws(Exception::class)
    override fun testFinished(description: Description?) {
        super.testFinished(description)
        reportSpecification(TraceEntry.TestResult.Passed,
                description!!.displayName)
    }

    @Throws(Exception::class)
    override fun testFailure(failure: Failure?) {
        super.testFailure(failure)
        reportSpecification(TraceEntry.TestResult.Failed, failure!!
                .description
                .displayName)
    }

    @Throws(Exception::class)
    override fun testIgnored(description: Description?) {
        super.testIgnored(description)
        val specOptional = getSpecification(description!!)
        if (specOptional.isPresent) {
            currentSpecification.set(specOptional.get())
        }
        reportSpecification(TraceEntry.TestResult.Ignored,
                description.displayName)
    }

    private fun reportSpecification(result: TraceEntry.TestResult,
                                    displayName: String) {
        if (currentSpecification.get() == null) {
            return
        }
        val trace = currentSpecification.get()
        val entry = TraceEntry(trace, displayName, result)
        if (fileReport) {
            entries.add(entry)

        } else {
            // display to console
            try {
                val json = objectMapper.readValue(
                        objectMapper.writeValueAsString(entry),
                        TraceEntry::class.java)
                println(objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(json))
            } catch (e: IOException) {
                println("Error writing as JSON")
                e.printStackTrace()
            }

        }
        currentSpecification.remove()
    }
}
