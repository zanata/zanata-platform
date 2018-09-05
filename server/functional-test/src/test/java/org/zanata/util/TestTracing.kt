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

import com.google.common.base.MoreObjects.firstNonNull
import org.codehaus.jackson.map.ObjectMapper
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.io.IOException
import java.util.Optional

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TestTracing : TestExecutionListener {

    private var fileReport: Boolean = false
    private val currentSpecification = ThreadLocal<Trace>()
    private var report: File? = null
    private val objectMapper = ObjectMapper()
    private val entries = ArrayList<TraceEntry>()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        val locationPath = System.getProperty("traceLocation",
                "./target/traceability")
        val location = File(locationPath)
        if (!location.exists() && !location.mkdirs()) {
            throw RuntimeException("Unable to create traceability report dir")
        }
        this.report = File(location, "traceability.json").also { report ->
            if (report.exists() && !report.delete()) {
                throw RuntimeException("Unable to remove traceability report")
            }
            fileReport = report.createNewFile()
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(report, entries)
        this.report = null
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        getSpecification(testIdentifier).ifPresent { spec ->
            currentSpecification.set(spec)
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        findTestMethod(testIdentifier).ifPresent { testMethod ->
            val name = getQualifiedName(testMethod)
            when (testExecutionResult.status!!) {
                TestExecutionResult.Status.SUCCESSFUL ->
                    reportSpecification(TraceEntry.TestResult.Passed, name)
                TestExecutionResult.Status.FAILED ->
                    reportSpecification(TraceEntry.TestResult.Failed, name)
                TestExecutionResult.Status.ABORTED -> {
                    getSpecification(testIdentifier).ifPresent { spec ->
                        currentSpecification.set(spec)
                    }
                    // technically aborted is not ignored
                    reportSpecification(TraceEntry.TestResult.Ignored, name)
                }
            }
        }
    }

    /**
     * annotation on method takes precedence over annotation on class. At the
     * moment class level annotation will cause multiple entries in the result
     * if there are more than one test methods.
     */
    private fun getSpecification(testIdentifier: TestIdentifier): Optional<Trace> {
        return findTestMethod(testIdentifier).flatMap { testMethod ->
            val testClass = testMethod.declaringClass
            val testClassTrace = testClass.getAnnotation(Trace::class.java)
            val testMethodTrace = testMethod.getAnnotation(Trace::class.java)
            return@flatMap Optional.ofNullable(firstNonNull(testMethodTrace, testClassTrace))
        }
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
