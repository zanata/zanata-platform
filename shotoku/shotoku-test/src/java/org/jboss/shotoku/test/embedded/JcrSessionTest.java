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
package org.jboss.shotoku.test.embedded;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.xml.sax.InputSource;

import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.jcr.Session;
import javax.jcr.NodeIterator;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrSessionTest {
    private static Repository getRepository() throws Exception {
        return RepositoryImpl.create(RepositoryConfig.create(
                new InputSource("/path/to/repository.xml"),
                "/local/jcr/path"));
    }

    private static Session getSession(Repository repo) throws Exception {
        return repo.login(new SimpleCredentials("username", "password".toCharArray()));
    }

    private static void printNodeNames(NodeIterator ni) throws Exception {
        while (ni.hasNext()) {
            System.out.println(ni.nextNode().getName());
        }

        System.out.println("-------------");
    }

    public static void main(String[] args) throws Exception {
        Repository repo1 = getRepository();
        Repository repo2 = getRepository();

        Session s = getSession(repo1);
        s.getRootNode().addNode("XXX");
        s.save();
        s.logout();

        s = getSession(repo2);
        printNodeNames(s.getRootNode().getNodes());
        s.logout();

        Repository repo3 = getRepository();
        s = getSession(repo3);
        printNodeNames(s.getRootNode().getNodes());
        s.logout();

        s = getSession(repo1);
        printNodeNames(s.getRootNode().getNodes());
        s.logout();

        // Cleanup
        s = getSession(repo1);
        s.getRootNode().getNode("XXX").remove();
        s.save();
        s.logout();
    }
}
