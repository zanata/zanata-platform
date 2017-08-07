package org.zanata.arquillian

import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.test.spi.TestClass
import org.junit.runner.notification.RunNotifier
import org.zanata.arquillian.lifecycle.LifecycleExecuter

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class LifecycleArquillian(testClass: Class<*>) : Arquillian(testClass) {
    override fun run(notifier: RunNotifier?) = LifecycleExecuter.withTestClass(TestClass(testClass.javaClass)) {
        super.run(notifier)
    }
}
