package org.zanata.security;

import java.util.Arrays;
import java.util.List;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jboss.seam.security.EntityAction;
import org.jboss.seam.security.EntitySecurityListener;
import org.jboss.seam.security.Identity;
import org.zanata.annotation.EntityRestrict;

/**
 * Overrides EntitySecurityListener to avoid calling
 * EntityPermissionChecker.instance() when security is disabled, which leads to
 * problems if there is no active Seam application context (in tests).
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class SmartEntitySecurityListener extends EntitySecurityListener {
    @PostLoad
    public void postLoad(Object entity) {
        if (Identity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.READ)) {
                super.postLoad(entity);
            }
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (Identity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.INSERT)) {
                super.prePersist(entity);
            }
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (Identity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.UPDATE)) {
                super.preUpdate(entity);
            }
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (Identity.isSecurityEnabled()) {
            if (isEntityRestricted(entity, EntityAction.DELETE)) {
                super.preRemove(entity);
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
            return false; // restricted, just not specifically
        }
    }

}
