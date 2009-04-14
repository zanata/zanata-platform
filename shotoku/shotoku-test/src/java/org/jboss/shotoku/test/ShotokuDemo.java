package org.jboss.shotoku.test;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.aop.Inject;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import static java.lang.System.out;

import java.util.Map;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ShotokuDemo {
	@Inject
    private ContentManager cm;

	public void printNode(String pathToNode) {
        try {
            Node node = cm.getNode(pathToNode);

            out.println("Content of node" + pathToNode + ": ");
            out.println(node.getContent());

            Map<String, String> properties = node.getProperties();
            out.println("Properties of node " + pathToNode + ": ");
            for (String propName : properties.keySet()) {
                out.println(propName + " = " + properties.get(propName));
            }
        } catch (ResourceDoesNotExist resourceDoesNotExist) {
            System.out.println("The given node does not exist: " + pathToNode + ".");
        }

    }
}















