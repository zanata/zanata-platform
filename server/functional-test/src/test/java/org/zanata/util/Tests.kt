package org.zanata.util

import org.junit.platform.commons.util.ReflectionUtils
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestIdentifier
import java.lang.reflect.Method
import java.util.Optional

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
fun findTestMethod(testIdentifier: TestIdentifier): Optional<Method> {
    return testIdentifier.source.flatMap { source ->
        if (source is MethodSource) {
            ReflectionUtils.findMethod(Class.forName(source.className), source.methodName, source.methodParameterTypes)
        } else Optional.empty<Method>()
    }
}

fun getQualifiedName(method: Method) =
        "${method.declaringClass.canonicalName}.${method.name}"
