package org.zanata.arquillian

import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.test.spi.TestClass
import org.junit.runner.notification.RunNotifier
import org.zanata.arquillian.lifecycle.LifecycleExecuter

/**
 * Based on the Arquillian test runner, but also tells LifecycleExecuter the
 * identity of the currently executing test class so that all the events in
 * [org.zanata.arquillian.lifecycle.api] will work properly.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class LifecycleArquillian(testClass: Class<*>) : Arquillian(testClass) {
    override fun run(notifier: RunNotifier?) = LifecycleExecuter.withTestClass(TestClass(testClass.javaClass)) {
        super.run(notifier)
    }
}
