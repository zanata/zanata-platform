package org.zanata.arquillian

import org.jboss.shrinkwrap.api.asset.ClassAsset
import org.jboss.shrinkwrap.api.asset.StringAsset
import org.jboss.shrinkwrap.api.container.ClassContainer
import org.jboss.shrinkwrap.api.container.ResourceContainer
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.descriptor.api.Descriptor
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor
import org.jboss.shrinkwrap.impl.base.path.BasicPath
import org.slf4j.LoggerFactory

/**
 * This object contains extension methods for Arquillian ClassContainer
 * (including WebArchive).
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

    /**
     * ClassContainer extension method which can add a list of classes and
     * the transitive set of classes they reference (based on analysis of
     * byte code). This includes supertypes, annotations, fields, method
     * signature types and method bodies.
     */
    @JvmOverloads
    @JvmStatic
    fun <T: ClassContainer<T>> T.addClassesWithDependencies(classes: List<Class<*>>, filter: ClassNameFilter = IN_ZANATA): T {
        val allClasses = findAllClassDependencyChains(classes, filter = filter) //.toTypedArray()
        log.info("Adding classes with dependencies: {} ({} total)", classes, allClasses.size)

        // uncomment if you want to see the classes and how they were referenced
//        allClasses.values.forEach{
//            println(it.reverse().map { it.simpleName }.joinToString("/"))
//        }

        allClasses.keys.forEach { clazz ->
            val asset = ClassAsset(clazz)
            val filename = clazz.name.replace('.','/')
            addAsResource(asset, BasicPath("$filename.class"))
        }

        return this
    }

    @JvmStatic
    fun <T: ResourceContainer<T>> T.addPersistenceConfig(): T {
        addAsResource("arquillian/persistence.xml", "META-INF/persistence.xml")
        addAsResource("META-INF/orm.xml")
        addAsResource("import.sql")
        return this
    }

    @JvmStatic
    fun WebArchive.addWebInfXml(xmlDescriptor: Descriptor): WebArchive {
        // contents of XML file
        val stringAsset = StringAsset(xmlDescriptor.exportAsString())
        // eg beans.xml, jboss-deployment-structure.xml:
        val path = xmlDescriptor.descriptorName
        addAsWebInfResource(stringAsset, path)
        return this
    }
}
