/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.shotoku.jcr;

import javax.jcr.*;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrTools {
    // Property names (configuration)
    static final String PROPERTY_CONNECTOR   = "connector";

    // JCR-Shotoku properties
    static final String JCR_PROP_LOG         = "log";
    static final String JCR_PROP_LAST_MOD    = "lastmod";
    static final String JCR_PROP_CREATED     = "created";

    static void safeSessionLogout(Session session) {
        if (session != null) {
            session.logout();
        }
    }

    static String removeNamespace(String s) {
        int colon = s.indexOf(':');
        if (colon == -1) {
            return s;
        } else {
            return s.substring(colon + 1);
        }
    }

    // TODO remove
    public void printProperties(String ind, javax.jcr.Node n) throws javax.jcr.RepositoryException {
        PropertyIterator pi = n.getProperties();
        System.out.println(ind + "Properties of: " + n.getName());
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            try {
                System.out.println(ind + p.getName() + " : " + p.getValue().getString());
            } catch (ValueFormatException e) {
                System.out.println(ind + p.getName() + " : multi values");
            }
        }
        System.out.println("----------");

        NodeIterator ni = n.getNodes();
        while (ni.hasNext()) {
            printProperties(ind + "  ", ni.nextNode());
        }
    }

}
