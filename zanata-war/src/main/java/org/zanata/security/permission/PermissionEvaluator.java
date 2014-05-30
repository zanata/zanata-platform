/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.security.permission;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.zanata.security.SecurityFunctions;

import com.google.common.collect.Lists;

/**
 * Holds all application permissions and provides a way to evaluate these
 * permissions for an object and an action.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("permissions")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Startup
public class PermissionEvaluator {

    private final List<Method> permissionFunctions = Lists.newArrayList();

    @Create
    public void buildIndex() {
        analyze(PermissionEvaluator.class);
        analyze(SecurityFunctions.class);
    }

    public void analyze(Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(ResolvesPermissions.class)) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new RuntimeException("Permission method "
                            + m.getName() + " must be static");
                } else if (m.getReturnType() != Boolean.class &&
                        m.getReturnType() != boolean.class) {
                    throw new RuntimeException("Permission method "
                            + m.getName() + " must return a Boolean type");
                }

                permissionFunctions.add(m);
            }
        }
    }

    public boolean evaluatePermission(String action, Object ... targets) {
        // Get the permissions to evaluate
        for (Method evaluatorMethod : permissionFunctions) {
            if( evaluatorMethodApplies(evaluatorMethod, action, targets) ) {
                try {
                    Object[] args =
                        prepareEvaluatorArgs(evaluatorMethod, action, targets);

                    Object result = evaluatorMethod.invoke(null, args);
                    if((Boolean)result) {
                        return true;
                    }
                }
                catch (IllegalArgumentException e) {
                    // Permission denied if the expected arguments are not
                    // passed
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                catch (InvocationTargetException e) {
                    // Permission denied if there's an exception thrown by the
                    // evaluator method
                }
            }
        }
        return false;
    }

    /**
     * Prepares the arguments to be passed to an evaluator method.
     */
    private Object[] prepareEvaluatorArgs(Method evaluatorMethod, String action,
        Object ... targets) {
        Class<?>[] argTypes = evaluatorMethod.getParameterTypes();
        Annotation[][] argAnnotations =
            evaluatorMethod.getParameterAnnotations();
        Object[] args = new Object[argTypes.length];
        for(int i=0, j=0; i < args.length; i++) {
            if(argTypes[i] == String.class && containsActionAnnotation(argAnnotations[i])) {
                // inject the action
                args[i] = action;
            }
            else {
                args[i] = findSuitableTarget(argTypes[i], targets);
            }
        }
        return args;
    }

    /**
     * Tries to find a suitable target expected for an evaluator.
     * Currently this implementation will return the first target that matches
     * the expected type. This will present problems if there are multiple
     * targets of the same type.
     * @param type The type of target expected.
     * @param targets The array of targets to choose from.
     * @return A target that is suitable to be passed as argument to the
     * evaluator method. The object return will be of class type. If a
     * suitable target is not found, null will be returned.
     */
    private Object findSuitableTarget(Class<?> type, Object ... targets) {
        for( Object t : targets ) {
            if( t.getClass() == type ) {
                return t;
            }
        }
        return null;
    }

    @VisibleForTesting
    boolean evaluatorMethodApplies(Method evaluator, String action, Object ... targets) {
        ResolvesPermissions permAnn =
                evaluator.getAnnotation(ResolvesPermissions.class);
        Class[] evaluatorTypes = getActualParamTypes(evaluator);
        Class[] targetTypes = new Class[targets.length];
        for( int i=0; i < targets.length; i++ ) {
            targetTypes[i] = targets[i].getClass();
        }

        boolean evaluates = true;
        // If The evaluator specifies an action, then it must match exactly
        if( permAnn.action().length > 0 ) {
            evaluates = evaluates && ArrayUtils.contains(permAnn.action(), action);
            if( !evaluates ) { // Quick return
                return false;
            }
        }
        // otherwise, it will evaluate it always

        return evaluates;
    }

    private Class[] getActualParamTypes(Method evaluator) {
        List<Class> actualParamTypes = Lists.newArrayList();
        Class[] evaluatorParamTypes = evaluator.getParameterTypes();
        Annotation[][] evaluatorParamAnnots =
            evaluator.getParameterAnnotations();

        for( int i=0; i < evaluatorParamTypes.length; i++ ) {
            if( evaluatorParamTypes[i] != String.class ||
                !containsActionAnnotation(evaluatorParamAnnots[i]) ) {
                actualParamTypes.add(evaluatorParamTypes[i]);
            }
        }
        return actualParamTypes.toArray(new Class[]{});
    }

    private boolean containsActionAnnotation(Annotation[] annotations) {
        for( Annotation a : annotations ) {
            if( a instanceof Action ) {
                return true;
            }
        }
        return false;
    }
}
