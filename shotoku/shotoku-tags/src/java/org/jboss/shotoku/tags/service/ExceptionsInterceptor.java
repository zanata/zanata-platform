package org.jboss.shotoku.tags.service;

import org.jboss.shotoku.tags.exceptions.TagAddException;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ExceptionsInterceptor {
    @AroundInvoke
    public Object catchExceptionsInterceptor(
            InvocationContext invocation) throws Exception {
        try {
            System.out.println("INTERCEPTOR");
            return invocation.proceed();
        } catch (Exception e) {
            System.out.println("Exception caught (2): " + e.getMessage());
            throw new TagAddException(e);
        }
    }
}
