package org.zanata.security;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SimpleGroup implements Group, Serializable {
    private static final long serialVersionUID = -1630229935369539932L;
    /**
     * The name of the group
     */
    private String name;

    /**
     * The members of this group
     */
    private Set<Principal> members = new HashSet<>();

    public SimpleGroup(String name) {
        this.name = name;
    }

    public boolean addMember(Principal user) {
        return members.add(user);
    }

    public boolean isMember(Principal member) {
        if (members.contains(member)) {
            return true;
        } else {
            for (Principal m : members) {
                if (m instanceof Group && ((Group) m).isMember(member)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(members);
    }

    public boolean removeMember(Principal user) {
        return members.remove(user);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleGroup) {
            SimpleGroup other = (SimpleGroup) obj;
            return other.name.equals(name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
