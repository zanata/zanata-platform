package org.zanata.webtrans.server;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Specifies the Action for the given action handler
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