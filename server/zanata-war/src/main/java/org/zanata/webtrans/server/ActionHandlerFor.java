package org.zanata.webtrans.server;

import net.customware.gwt.dispatch.shared.Action;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the Action for the given ActionHandler
 */
@Target({ TYPE, METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
@Qualifier
@Inherited
public @interface ActionHandlerFor {
    /**
     * @return the component name
     */
    Class<? extends Action<?>> value();
}
