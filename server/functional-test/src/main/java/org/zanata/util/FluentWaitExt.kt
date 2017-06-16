package org.zanata.util

import org.openqa.selenium.support.ui.FluentWait

/**
 * Waits for the specified function to be non-null/non-false, using the
 * specified message in case of failure, and clearing the FluentWait's
 * message afterwards.
 *
 * ```kotlin
 * wait.until("displayed") { it.isDisplayed }
 * ```
 */
fun <T, V> FluentWait<T>.until(message: String, isTruthy: (T) -> V): V {
    return until({ message }, isTruthy)
}

/**
 * Waits for the specified function to be non-null/non-false, using the
 * specified message function in case of failure, and clearing the
 * FluentWait's message afterwards.
 *
 * ```kotlin
 * wait.until({ "displayed" }, { it.isDisplayed })
 * ```
 */
fun <T, V> FluentWait<T>.until(messageSupplier: () -> String, isTruthy: (T) -> V): V {
    withMessage(messageSupplier)
    try {
        return this.until(isTruthy)
    } finally {
        withMessage { null }
    }
}

//fun <T> FluentWait<T>.until(message: String, predicate: Predicate<T>) {
//    return until(message) {
//        it -> predicate.test(it)
//    }
//}

/*
fun example1(wait: FluentWait<WebElement>) {
    wait.until("displayed") { it.isDisplayed }
}

fun example2(wait: FluentWait<WebElement>) {
    wait.until({ "displayed" }, { it.isDisplayed })
}
*/
