package org.zanata.arquillian

import org.jboss.shrinkwrap.api.asset.ClassAsset
import org.jboss.shrinkwrap.api.container.ClassContainer
import org.jboss.shrinkwrap.impl.base.path.BasicPath
import org.slf4j.LoggerFactory

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
object ArquillianUtil {
    private val log = LoggerFactory.getLogger(ArquillianUtil::class.java)
    private val TOP_PACKAGE = "org.zanata"
    private fun inAZanataPackage(className: String): Boolean = className.startsWith(TOP_PACKAGE)
    val IN_ZANATA: ClassNameFilter = ArquillianUtil::inAZanataPackage

    private fun notInPlatform(className: String): Boolean {
        // TODO listing the packages which will be provided by the platform is a losing battle
        val blacklist = arrayOf("org.jboss", "org.hibernate", "java", "com.sun", "sun", "org.w3c", "org.xml", "org.codehaus.jackson", "org.apache.log4j", "org.wildfly", "org.picket", "org.infinispan")
//        return !(className.startsWith("org.jboss") || className.startsWith("org.hibernate") || className.startsWith("java") || className.startsWith("com.sun") || className.startsWith("sun") || className.startsWith("org.w3c") || className.startsWith("org.xml") || className.startsWith("org.codehaus.jackson") || className.startsWith("org.apache.log4j") || className.startsWith("org.wildfly") || className.startsWith("org.picket") || className.startsWith("org.infinispan"))
        blacklist.forEach {
            if (className.startsWith(it)) return false
        }
        return true
    }
    val NOT_IN_PLATFORM: ClassNameFilter = ArquillianUtil::notInPlatform

    @JvmOverloads
    @JvmStatic
    fun <T: ClassContainer<T>> T.addClassesWithSupertypes(vararg classes: Class<*>, filter: ClassNameFilter = IN_ZANATA): T {
        classes
                .filter { filter(it.name) }
                .forEach { clazz ->
                    addClass(clazz)
                    clazz.superclass?.let { addClassesWithSupertypes(it, filter = filter) }
                    clazz.interfaces.forEach { addClassesWithSupertypes(it, filter = filter) }
                }
        return this
    }

    // ie supertypes, annotations, fields, method signature types
    @JvmOverloads
    @JvmStatic
    fun <T: ClassContainer<T>> T.addClassesWithDependencies(vararg classes: Class<*>, filter: ClassNameFilter = IN_ZANATA): T {
        val allClasses = findAllClassDependencies(*classes, filter = filter) //.toTypedArray()
        log.info("Adding classes with dependencies: {} ({} total)", classes, allClasses.size)

        allClasses.forEach { clazz ->
            val asset = ClassAsset(clazz)
            val filename = clazz.name.replace('.','/')
            addAsResource(asset, BasicPath("$filename.class"))
        }

        return this
    }

}
