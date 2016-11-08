package org.zanata.security;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class Role extends SimplePrincipal {
    private static final long serialVersionUID = 6942262597465239358L;
    private boolean conditional;

    public Role(String name) {
        super(name);
    }

    public Role(String name, boolean conditional) {
        super(name);
        this.conditional = conditional;
    }

    public boolean isConditional() {
        return conditional;
    }
}
