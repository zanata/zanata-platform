// Implementation copied from Seam 2.3.1

package org.zanata.seam.framework;

import java.io.Serializable;

import javax.transaction.Status;

import org.jboss.seam.Component;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;

/**
 * Base class for controller objects. Provides various helper methods that help
 * slightly reduce the code required to create a Seam component that acts as a
 * controller.
 *
 * @author Gavin King
 */
public abstract class Controller implements Serializable {

    protected Conversation getConversation() {
        return Conversation.instance();
    }

    protected Object getComponentInstance(String name) {
        return Component.getInstance(name);
    }

    protected boolean isTransactionMarkedRollback() {
        try {
            return getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (Exception e) {
            return false;
        }
    }

    private UserTransaction getTransaction() {
        return Transaction.instance();
    }

}
