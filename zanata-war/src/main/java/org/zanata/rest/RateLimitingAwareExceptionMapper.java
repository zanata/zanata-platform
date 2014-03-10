package org.zanata.rest;


/**
 * This should be the base of ALL REST exception mapper once we introduce rate
 * limiting.
 *
 * Note: I can't use template method pattern here to enforce all subclass has
 * called releaseSemaphore method in toResponse method. RESTeasy looks at
 * generic interface and grab the type out of there. Child class has to provide
 * a toResponse method with exact exception as argument. Therefore this class
 * mainly serve as an marker mechanism where there is a test to scan all
 * ExceptionMapper implementation and ensure they are all subclass of this type
 * (so that when developers implement new mapper they won't forget to call
 * release semaphore)
 *
 * @see <a href=
 *      "http://stackoverflow.com/questions/13937998/resteasy-post-process-interceptor-chain-not-traversed-when-response-created-by-e">RestEasy
 *      Post Process Interceptor chain not traversed when response created by
 *      ExceptionMapper</a>
 *
 * @see org.jboss.resteasy.spi.ResteasyProviderFactory#addExceptionMapper(javax.ws.rs.ext.ExceptionMapper, java.lang.Class)
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class RateLimitingAwareExceptionMapper {

    /**
     * Just remember to call this method before returning your Response.
     */
    protected void releaseSemaphoreBeforeReturnResponse() {
        RateLimiterHolder.getInstance().releaseSemaphoreForCurrentThread();
    }
}
