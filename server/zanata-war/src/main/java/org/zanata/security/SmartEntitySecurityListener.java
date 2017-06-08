package org.zanata.security;

import java.util.Arrays;
import java.util.List;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.zanata.annotation.EntityRestrict;

/**
 * Disable permission checking when security is disabled, which solves the
 * problems if there is no active Seam application context (in tests).
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class SmartEntitySecurityListener {
    @PostLoad
    public void postLoad(Object entity) {
        if (ZanataIdentity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.READ)) {
                checkEntityPermission(entity, EntityAction.READ);
            }
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (ZanataIdentity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.INSERT)) {
                checkEntityPermission(entity, EntityAction.INSERT);
            }
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (ZanataIdentity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.UPDATE)) {
                checkEntityPermission(entity, EntityAction.UPDATE);
            }
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (ZanataIdentity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.DELETE)) {
                checkEntityPermission(entity, EntityAction.DELETE);
            }
        }
    }

    private static boolean
            isEntityRestricted(Object entity, EntityAction action) {
        EntityRestrict entityRestrict =
                entity.getClass().getAnnotation(EntityRestrict.class);
        if (entityRestrict != null) {
            List<EntityAction> restrictedActions = Arrays.asList(
                    entityRestrict.value());
            if (restrictedActions.isEmpty() || restrictedActions.contains(action)) {
                return true; // restricted
            } else {
                return false; // not restricted
            }
        } else {
            return false; // not restricted, just not specifically
        }
    }

    @SuppressWarnings("deprecation")
    public void checkEntityPermission(Object entity, EntityAction action) {
        if (!ZanataIdentity.isSecurityEnabled()) {
            return;
        }

        if (!org.zanata.util.Contexts.isSessionContextActive()) {
            return;
        }

        ZanataIdentity identity = ZanataIdentity.instance();

        identity.tryLogin();
        identity.checkPermission(entity, action.toString().toLowerCase());
    }

}
