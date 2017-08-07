package org.zanata.arquillian

import org.apache.maven.shared.dependency.analyzer.asm.DependencyClassFileVisitor
import org.slf4j.LoggerFactory
import org.zanata.webtrans.shared.model.GlossaryResultItem
import java.io.InputStream

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
typealias ClassNameFilter = (String) -> Boolean

private object Classes
private val log = LoggerFactory.getLogger(Classes::class.java)

fun findAllClassDependencies(vararg classes: Class<*>, filter: ClassNameFilter): Set<Class<*>> {
    var nextDeps = mutableSetOf<Class<*>>()
    val analysedDeps = mutableSetOf<Class<*>>()

    var currentDeps = classes.iterator()
    while (true) {
        if (!currentDeps.hasNext()) {
            if (nextDeps.isEmpty()) break
            currentDeps = nextDeps.iterator()
            nextDeps = mutableSetOf<Class<*>>()
        }
        val clazz = currentDeps.next()
        analysedDeps.add(clazz)
        val newDeps = findClassDependencies(clazz)
                .filter(filter)
                .mapNotNull {
                    loadClass(it)
                }
                .subtract(analysedDeps)
        nextDeps.addAll(newDeps)
    }

    return analysedDeps
}

fun loadClass(className: String): Class<*>? {
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

val Class<*>.basicComponentType: Class<*>
    get() =
    if (isArray) componentType.basicComponentType
    else this

fun ClassLoader?.resourceAsStream(name: String): InputStream? =
        if (this != null) getResourceAsStream(name)
        else ClassLoader.getSystemResourceAsStream(name)

val Class<*>.inputStream: InputStream?
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
 * Currently uses maven-dependency-analyzer (ASM).
 * May include Strings which aren't class names (because
 * maven-dependency-analyzer aggressively inspects the constant pool and
 * sometimes returns Strings which aren't class names).
 */
fun findClassDependencies(clazz: Class<*>): Set<String> {
    log.debug("findClassDependencies({}), clazz")
    val visitor = DependencyClassFileVisitor()
    clazz.inputStream?.use {
        // NB visitClass doesn't close the InputStream
        visitor.visitClass(clazz.name, it)
    }

    // The class itself doesn't refer to the package info, but it may contain
    // package annotations which will affect runtime behaviour (eg JAXB)
    val packageInfo = loadClass(clazz.`package`.name + ".package-info")
    if (packageInfo != null) {
        return visitor.dependencies + packageInfo.name
    } else {
        return visitor.dependencies
    }
}
/*
fun main(args: Array<String>) {
    findClassDependencies(GlossaryResultItem::class.java).forEach { println(it) }
}
*/
