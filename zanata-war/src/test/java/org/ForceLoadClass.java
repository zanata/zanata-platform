package org;

import org.zanata.seam.SeamAutowire;
import com.google.common.base.Throwables;

/**
 * This class is to make sure SeamAutowire is loaded earlier. If class
 * org.jboss.seam.contexts.Contexts is loaded before SeamAutoWire, there will be
 * a nasty class loading exception.
 *
 * This seems to happen at random (i.e. failed on jenkins but not on my local
 * machine or vice versa)
 *
 * <pre>
 * An error occurred while instantiating class org.zanata.rest.service.CopyTransRestTest: null
 * ...
 *     at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:103)
 * Caused by: java.lang.ExceptionInInitializerError
 *     at org.zanata.ZanataRestTest.<clinit>(ZanataRestTest.java:46)
 *     at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
 *     at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
 *     at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
 *     at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
 *     at org.testng.internal.ObjectFactoryImpl.newInstance(ObjectFactoryImpl.java:29)
 *     at org.testng.internal.ClassHelper.createInstance1(ClassHelper.java:387)
 *     ... 21 more
 * Caused by: java.lang.RuntimeException: Problem rewiring Seam's Contexts class
 *     at org.zanata.seam.SeamAutowire.rewireSeamContextsClass(SeamAutowire.java:378)
 *     at org.zanata.seam.SeamAutowire.<clinit>(SeamAutowire.java:85)
 *     ... 28 more
 * Caused by: javassist.CannotCompileException: by java.lang.LinkageError: loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name: "org/jboss/seam/contexts/Contexts"
 *     at javassist.ClassPool.toClass(ClassPool.java:1089)
 *     at javassist.ClassPool.toClass(ClassPool.java:1032)
 *     at javassist.ClassPool.toClass(ClassPool.java:990)
 *     at javassist.CtClass.toClass(CtClass.java:1125)
 *     at org.zanata.seam.SeamAutowire.rewireSeamContextsClass(SeamAutowire.java:373)
 *     ... 29 more
 * Caused by: java.lang.LinkageError: loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name: "org/jboss/seam/contexts/Contexts"
 *     at java.lang.ClassLoader.defineClass1(Native Method)
 *     at java.lang.ClassLoader.defineClass(ClassLoader.java:800)
 *     at java.lang.ClassLoader.defineClass(ClassLoader.java:643)
 *     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 *     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
 *     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 *     at java.lang.reflect.Method.invoke(Method.java:606)
 *     at javassist.ClassPool.toClass2(ClassPool.java:1102)
 *     at javassist.ClassPool.toClass(ClassPool.java:1083)
 *     ... 33 more
 * </pre>
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ForceLoadClass {
    static {
        try {
            Class.forName(SeamAutowire.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            throw Throwables.propagate(e);
        }
    }
}
