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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;
import org.zanata.util.BeanHolder;
import org.zanata.util.ServiceLocator;

/**
 * Represents a function that grants a permission.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class PermissionGranter {
    private final Method granterMethod;
    private Collection<String> evaluatedActions;
    private List<Class<?>> acceptedParameterTypes;
    private int actionParameterIndex = -1;

    public PermissionGranter(Method granterMethod) {
        this.granterMethod = granterMethod;
        this.populateFields();
    }

    /**
     * Validates that the permission granter satisfies all preconditions.
     */
    public void validate() {
        if (granterMethod.getReturnType() != Boolean.class
                && granterMethod.getReturnType() != boolean.class) {
            throw new RuntimeException("Permission method "
                    + granterMethod.getName() + " must return a Boolean type");
        }
    }

    private void populateFields() {
        GrantsPermission grantAnn =
                granterMethod.getAnnotation(GrantsPermission.class);
        evaluatedActions = Lists.newArrayList(grantAnn.actions());
        acceptedParameterTypes =
                Lists.newArrayList(granterMethod.getParameterTypes());
        Annotation[][] granterParamAnns =
                granterMethod.getParameterAnnotations();
        for (int i = 0; i < acceptedParameterTypes.size(); i++) {
            if (acceptedParameterTypes.get(i) == String.class
                    && containsActionAnnotation(granterParamAnns[i])) {
                actionParameterIndex = i;
                break; // Only one action parameter index is allowed
            }
        }
    }

    private boolean containsActionAnnotation(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a instanceof Action) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the granter should be invoked for the given targets.
     *
     * @param targets
     *            Permission check targets
     * @return True if the granter should be invoked for the given set of
     *         targets.
     */
    public boolean shouldInvokeGranter(Object... targets) {
        // Only invoke the granter if all its parameters can be provided
        int paramIdx = 0;
        for (Class<?> paramType : acceptedParameterTypes) {
            boolean foundParameter = false;
            if (paramIdx == actionParameterIndex && paramType == String.class) {
                foundParameter = true; // Action parameter can always be
                // injected
            } else {
                for (Object t : targets) {
                    if (t != null && paramType.isAssignableFrom(t.getClass())) {
                        foundParameter = true;
                        break;
                    }
                }
            }
            // If a matching parameter cannot be found, then the granter will
            // not
            // be executed
            if (!foundParameter) {
                return false;
            }
            paramIdx++;
        }
        return true;
    }

    /**
     * Invokes the granter.
     *
     * @param action
     *            The action that is being checked for permissions.
     * @param targets
     *            The target object instances that the action will apply to.
     * @return True, if the permission to perform the action on the targets has
     *         been granted. False otherwise.
     */
    public boolean invoke(String action, Object... targets) {
        Class componentClass = granterMethod.getDeclaringClass();
        Object[] granterParams = new Object[acceptedParameterTypes.size()];
        int paramIdx = 0;
        for (Class<?> paramType : acceptedParameterTypes) {
            if (paramIdx == actionParameterIndex) {
                // Inject the action
                granterParams[paramIdx] = action;
            } else {
                granterParams[paramIdx] =
                        findParameterForClass(targets, paramType);
            }
            paramIdx++;
        }
        try (BeanHolder componentHolder =
                ServiceLocator.instance().getDependent(componentClass)) {
            return (Boolean) granterMethod.invoke(componentHolder.get(),
                    granterParams);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object findParameterForClass(Object[] params, Class<?> paramType) {
        for (Object p : params) {
            if (paramType.isAssignableFrom(p.getClass()))
                return p;
        }
        return null;
    }

    @Override
    public String toString() {
        return "PermissionGranter("
                + granterMethod.getDeclaringClass().getSimpleName() + "."
                + granterMethod.getName() + ")";
    }

    public Collection<String> getEvaluatedActions() {
        return this.evaluatedActions;
    }

    public List<Class<?>> getAcceptedParameterTypes() {
        return this.acceptedParameterTypes;
    }
}
