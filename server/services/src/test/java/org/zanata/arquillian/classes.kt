// TODO fix this in pom.xml
@file:Suppress("INTERFACE_STATIC_METHOD_CALL_FROM_JAVA6_TARGET")

package org.zanata.arquillian

import cyclops.collections.immutable.LinkedListX
import org.apache.maven.shared.dependency.analyzer.asm.DependencyClassFileVisitor
import org.jooq.lambda.Seq
import org.slf4j.LoggerFactory
import org.zanata.arquillian.ArquillianUtil.IN_ZANATA
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo
import java.io.InputStream

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
typealias ClassNameFilter = (String) -> Boolean

private object Classes
private val log = LoggerFactory.getLogger(Classes::class.java)

fun findAllClassDependencies(classes: List<Class<*>>, filter: ClassNameFilter): Set<Class<*>> {
    return findAllClassDependencyChains(classes, filter = filter).keys
}

/**
 * Returns a list of all classes transitively required by the specified classes
 * @param classes list of classes whose dependencies are needed
 * @param filter a filter which is used to prune the set of classes
 * @return a `Map` where each key is a dependency `Class`, and the value is a
 * `Seq<Class>` representing the dependency chain which led to that `Class`
 * (in reverse, with the `Class` in question at the *front*).
 */
fun findAllClassDependencyChains(classes: List<Class<*>>, filter: ClassNameFilter): Map<Class<*>, Seq<Class<*>>> {
    var nextDeps = mutableSetOf<Seq<Class<*>>>()
    val analysedDeps = mutableMapOf<Class<*>, Seq<Class<*>>>()

    var currentDeps: Iterator<Seq<Class<*>>> = classes.map { Seq.of(it) }.iterator()
    while (true) {
        if (!currentDeps.hasNext()) {
            if (nextDeps.isEmpty()) break
            currentDeps = nextDeps.iterator()
            nextDeps = mutableSetOf()
        }
        val clazzes: Seq<Class<*>> = currentDeps.next()
        val clazz = clazzes.findFirst().get()
        analysedDeps[clazz] = clazzes
        val newDeps: Set<Class<*>> = findDirectClassDependencies(clazz)
                .filter(filter)
                .mapNotNull {
                    loadBasicClass(it)
                }
                .subtract(analysedDeps.keys)
        nextDeps.addAll(newDeps.map { clazzes.prepend(it) })
    }

    return analysedDeps
}

/**
 * Tries to load the class and returns the nearest "object" class.
 * If class is primitive or non-existent, returns null.
 * If class is an array, returns the underlying element type.
 */
private fun loadBasicClass(className: String): Class<*>? {
    return try {
        val clazz = Class.forName(className, false, Classes.javaClass.classLoader)
        if (clazz.isArray) return clazz.basicComponentType
        if (clazz.isPrimitive) return null
        return clazz
    } catch (e: Throwable) {
        log.debug("Can't load class {}", className)
        null
    }
}

private val Class<*>.basicComponentType: Class<*>
    get() =
    if (isArray) componentType.basicComponentType
    else this

private fun ClassLoader?.resourceAsStream(name: String): InputStream? =
        if (this != null) getResourceAsStream(name)
        else ClassLoader.getSystemResourceAsStream(name)

private val Class<*>.inputStream: InputStream?
    get() {
        if (isPrimitive) return null
        val resourceName = this.name.replace('.', '/') + ".class"
        val inputStream = this.classLoader.resourceAsStream(resourceName)
        if (inputStream == null) {
            log.warn("Missing InputStream for {}", this)
            return null
        } else {
            return inputStream
        }
    }

/**
 * Returns a set of class names which are referenced by clazz's bytecode.
 * Currently uses maven-dependency-analyzer (based on ASM).
 * May include Strings which aren't class names (because
 * maven-dependency-analyzer aggressively inspects the constant pool and
 * sometimes returns Strings which aren't class names).
 */
private fun findDirectClassDependencies(clazz: Class<*>): Set<String> {
    log.debug("findDirectClassDependencies({})", clazz)
    val visitor = DependencyClassFileVisitor()
    clazz.inputStream?.use {
        // NB visitClass doesn't close the InputStream
        visitor.visitClass(clazz.name, it)
    }

    // The class itself doesn't refer to the package info, but it may contain
    // package annotations which will affect runtime behaviour (eg JAXB)
    clazz.`package`?.name?.let { pkg ->
        val packageInfo = loadBasicClass(pkg + ".package-info")
        packageInfo?.let { visitor.dependencies.add(it.name) }
    }
    return visitor.dependencies
}

fun main(args: Array<String>) {
    findAllClassDependencyChains(listOf(TransUnitUpdateInfo::class.java), filter = IN_ZANATA).values.forEach {
        println(it.reverse().map { it.simpleName }.joinToString("/"))
    }
}
