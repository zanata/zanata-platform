// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.transaction.Transaction;

/**
 * Base class for controller objects. Provides various helper methods that help
 * slightly reduce the code required to create a Seam component that acts as a
 * controller.
 *
 * @author Gavin King
 */
public abstract class Controller implements Serializable {

    protected Events getEvents() {
        return Events.instance();
    }

    protected Conversation getConversation() {
        return Conversation.instance();
    }

    protected StatusMessages getStatusMessages() {
        return StatusMessages.instance();
    }

    protected void raiseTransactionSuccessEvent(String type,
            Object... parameters) {
        getEvents().raiseTransactionSuccessEvent(type, parameters);
    }

    protected Object getComponentInstance(String name) {
        return Component.getInstance(name);
    }

    protected boolean isTransactionMarkedRollback() {
        try {
            return Transaction.instance().isMarkedRollback();
        } catch (Exception e) {
            return false;
        }
    }

}
