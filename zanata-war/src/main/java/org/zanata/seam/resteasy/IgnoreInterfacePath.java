package org.zanata.seam.resteasy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * Tells ZanataResteasyBootstrap to ignore the @Path annotation on any
 * interfaces in favour of the Bean itself.
 * @see org.zanata.rest.ZanataResteasyBootstrap#getAnnotatedInterface(Class, org.jboss.seam.Component)
 */
public @interface IgnoreInterfacePath
{
}
