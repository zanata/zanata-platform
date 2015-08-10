package org.zanata.seam.interceptor;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.zanata.exception.NotLoggedInException;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.jboss.seam.async.AsynchronousInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.util.Strings;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.util.ServiceLocator;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO [CDI] this will need to change to CDI interceptor once migrated to CDI
 *
 * Copy and modified from org.jboss.seam.security.SecurityInterceptor
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Interceptor(type = InterceptorType.CLIENT,
        around = AsynchronousInterceptor.class)
@Slf4j
public class ZanataSecurityInterceptor extends AbstractInterceptor
        implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * You may encounter a JVM bug where the field initializer is not evaluated
     * for a transient field after deserialization.
     *
     * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6252102"
     */
    private transient volatile Map<Method, Restriction> restrictions =
            new HashMap<>();

    private class Restriction {
        private Set<String> roleRestrictions;
        private boolean loggedInRestriction;

        public void addLoggedInRestriction() {
            this.loggedInRestriction = true;
        }

        public void addRoleRestriction(String role) {
            if (roleRestrictions == null) {
                roleRestrictions = new HashSet<>();
            }

            roleRestrictions.add(role);
        }

        public void check() {
            if (!ZanataIdentity.isSecurityEnabled()) {
                return;
            }

            if (loggedInRestriction) {
                if (!getIdentity().isLoggedIn()) {
                    throw new NotLoggedInException();
                }
            }

            if (roleRestrictions != null) {
                for (String role : roleRestrictions) {
                    getIdentity().checkRole(role);
                }
            }

        }

        private ZanataIdentity getIdentity() {
            return ServiceLocator.instance().getInstance(
                    ZanataIdentity.class);
        }
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocation) throws Exception {
        Method interfaceMethod = invocation.getMethod();

        if (!"hashCode".equals(interfaceMethod.getName())) {
            Restriction restriction = getRestriction(interfaceMethod);
            if (restriction != null) {
                restriction.check();
            }
        }

        return invocation.proceed();
    }

    private Restriction getRestriction(Method interfaceMethod) throws Exception {
        // see field declaration as to why this is done
        if (restrictions == null) {
            synchronized (this) {
                restrictions = new HashMap<>();
            }
        }

        if (!restrictions.containsKey(interfaceMethod)) {
            synchronized (restrictions) {
                // FIXME this logic should be abstracted rather than sitting in
                // the middle of this interceptor
                if (!restrictions.containsKey(interfaceMethod)) {
                    Restriction restriction = null;

                    Method method =
                            getComponent().getBeanClass().getMethod(
                                    interfaceMethod.getName(),
                                    interfaceMethod.getParameterTypes());

                    for (Annotation annotation : method.getDeclaringClass()
                            .getAnnotations()) {
                        if (annotation.annotationType().equals(
                                CheckRole.class)) {
                            if (restriction == null) {
                                restriction = new Restriction();
                            }
                            CheckRole checkRole =
                                    method.getDeclaringClass().getAnnotation(
                                            CheckRole.class);
                            restriction.addRoleRestriction(checkRole.value());
                        }
                    }

                    for (Annotation annotation : method.getAnnotations()) {

                        if (annotation.annotationType().equals(
                                CheckRole.class)) {
                            if (restriction == null) {
                                restriction = new Restriction();
                            }
                            CheckRole checkRole =
                                    method.getAnnotation(CheckRole.class);
                            restriction.addRoleRestriction(checkRole.value());
                        }
                    }

                    for (Annotation annotation : method.getDeclaringClass()
                            .getAnnotations()) {
                        if (annotation.annotationType().equals(
                                CheckLoggedIn.class)) {
                            if (restriction == null) {
                                restriction = new Restriction();
                            }

                            restriction.addLoggedInRestriction();
                        }
                    }

                    for (Annotation annotation : method.getAnnotations()) {

                        if (annotation.annotationType().equals(
                                CheckLoggedIn.class)) {
                            if (restriction == null) {
                                restriction = new Restriction();
                            }
                            restriction.addLoggedInRestriction();
                        }
                    }

                    restrictions.put(interfaceMethod, restriction);
                    return restriction;
                }
            }
        }
        return restrictions.get(interfaceMethod);
    }

    public boolean isInterceptorEnabled() {
        boolean enabled = getComponent().beanClassHasAnnotation(ZanataSecured.class);
        return enabled;
    }
}
