// This implementation is copied from com.binarytweed.test.QuarantiningRunner
// in https://github.com/BinaryTweed/quarantining-test-runner v0.0.2
// with quarantine disabled for testFileClassName and
// delegateRunningToClassName.

// TODO see if we can remove this class when this issue is closed:
// https://github.com/BinaryTweed/quarantining-test-runner/issues/3

package org.zanata.test;

import java.lang.reflect.InvocationTargetException;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import com.binarytweed.test.DelegateRunningToDiscoverer;
import com.binarytweed.test.QuarantinedPatternDiscoverer;
import com.binarytweed.test.QuarantiningUrlClassLoader;

/**
 * Uses {@code QuarantiningUrlClassLoader} to load the test class, meaning the
 * {@code Quarantine} annotation can be used to ensure certain classes are
 * loaded separately.
 *
 * Use of a separate class loader allows classes to be reloaded for each test
 * class, which is handy when you're testing frameworks that make use of static
 * members.
 *
 * The selective quarantining is required because if the test class and its
 * 'children' are all loaded by a different class loader, then the {@code Test}
 * annotations yield different {@code Class} instances. JUnit then thinks there
 * are no runnable methods, because it looks them up by Class.
 *
 * This implementation was modified (by Sean Flanigan) so that it no longer
 * implicitly quarantines the Test class and the delegate Runner.
 */
public class QuarantiningRunner extends Runner {
    private final Object innerRunner;
    private final Class<?> innerRunnerClass;
    private final DelegateRunningToDiscoverer delegateRunningToDiscoverer;
    private final QuarantinedPatternDiscoverer quarantinedPatternDiscoverer;

    public QuarantiningRunner(Class<?> testFileClass)
            throws InitializationError {
        delegateRunningToDiscoverer = new DelegateRunningToDiscoverer();
        quarantinedPatternDiscoverer = new QuarantinedPatternDiscoverer();
        Class<? extends Runner> delegateRunningTo =
                delegateRunningToDiscoverer
                        .getDelegateRunningToOn(testFileClass);

        String testFileClassName = testFileClass.getName();
        String delegateRunningToClassName = delegateRunningTo.getName();

        String[] quarantinedPatterns =
                quarantinedPatternDiscoverer
                        .getQuarantinedPatternsOn(testFileClass);
        // String[] allPatterns = Arrays.copyOf(quarantinedPatterns,
        // quarantinedPatterns.length + 2);
        // allPatterns[quarantinedPatterns.length] = testFileClassName;
        // allPatterns[quarantinedPatterns.length + 1] =
        // delegateRunningToClassName;
        //
        // QuarantiningUrlClassLoader classLoader = new
        // QuarantiningUrlClassLoader(allPatterns);
        QuarantiningUrlClassLoader classLoader =
                new QuarantiningUrlClassLoader(quarantinedPatterns);

        try {
            innerRunnerClass =
                    classLoader.loadClass(delegateRunningToClassName);
            Class<?> testClass = classLoader.loadClass(testFileClassName);
            innerRunner =
                    innerRunnerClass.cast(innerRunnerClass.getConstructor(
                            Class.class).newInstance(testClass));
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            throw new InitializationError(e);
        }
    }

    @Override
    public Description getDescription() {
        try {
            return (Description) innerRunnerClass.getMethod("getDescription")
                    .invoke(innerRunner);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException("Could not get description", e);
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            innerRunnerClass.getMethod("run", RunNotifier.class).invoke(
                    innerRunner, notifier);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        }
    }
}
