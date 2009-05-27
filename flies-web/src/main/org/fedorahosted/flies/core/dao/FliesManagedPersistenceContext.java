package org.fedorahosted.flies.core.dao;

import org.jboss.seam.persistence.ManagedPersistenceContext;
import org.jboss.seam.persistence.Filter;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.core.Events;

/**
 * Forces re-evaluation of filter parameter for managed persistence contexts.
 * <p>
 * This is called after a user logs in, so that the <tt>#{currentAccessLevel}</tt>
 * filter argument is evaluated again, <i>on the already open</i> persistence context
 * (which has the filter argument from before login). See the filter definition in
 * components.xml.
 * <p>
 * The risk with re-setting filters on an existing persistence context is that things
 * might be cached already that should be filtered out. Now, in our case that works
 * fine because a login always means that the current user gains privileges and
 * raises his/her access level, i.e. sees a superset of the data he has seen before.
 * 
 * @author Christian Bauer
 */
public class FliesManagedPersistenceContext extends ManagedPersistenceContext {

    @Logger
    Log log;

    @Observer(value = {"User.loggedIn", "User.loggedInBasicHttp"}, create = false)
    public void resetFilter() {
        try {

            log.debug("Resetting persistence context filters");
            PersistenceProvider persistenceProvider = PersistenceProvider.instance();
            for (Filter f : getFilters()) {
                if (f.isFilterEnabled()) {
                    persistenceProvider.enableFilter(f, getEntityManager());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Events.instance().raiseEvent("PersistenceContext.filterReset");
    }
}
