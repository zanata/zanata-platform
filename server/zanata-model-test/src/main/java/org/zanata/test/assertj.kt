package org.zanata.test

import org.assertj.core.description.Description
import java.util.function.Supplier

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
fun describedBy(supplier: Supplier<Any>?): Description {
    return object : Description() {
        override fun value(): String {
            return supplier?.get().toString()
        }
    }
}
