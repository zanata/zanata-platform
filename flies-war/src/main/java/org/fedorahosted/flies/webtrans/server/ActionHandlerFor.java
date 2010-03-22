package org.fedorahosted.flies.webtrans.server;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Specifies the component name of a Seam component.
 * 
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * 
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface ActionHandlerFor 
{
   /**
    * @return the component name
    */
   Class<? extends Action<?>> value();
}