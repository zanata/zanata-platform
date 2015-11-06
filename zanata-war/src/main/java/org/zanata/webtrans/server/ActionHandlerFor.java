package org.zanata.webtrans.server;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Specifies the Action for the given ActionHandler
 */
@Target({ TYPE, METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface ActionHandlerFor {
    /**
     * @return the component name
     */
    Class<? extends Action<?>> value();
}
