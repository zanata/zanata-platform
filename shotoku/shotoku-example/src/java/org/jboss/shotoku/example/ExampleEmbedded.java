package org.jboss.shotoku.example;

import org.jboss.shotoku.*;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.NameFormatException;
import org.jboss.shotoku.exceptions.SaveException;

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ExampleEmbedded {
    private ContentManager cm;
    private Set<Node> openNodes;
    private Set<Directory> openDirectories;

    public ExampleEmbedded() {
        // Initializing with the default content manager.
        cm = ContentManager.getContentManager();

        openNodes = new HashSet<Node>();
        openDirectories = new HashSet<Directory>();
    }

    /**
     * Changes the content manager to a one with the given id (should be
     * one of the  specified in shotoku.properties) and prefix (a path to
     * which all other used paths will be relative).
     * @param id
     * @param prefix
     */
    public void changeContentManager(String id, String prefix) {
        ContentManager newCm = ContentManager.getContentManager(id, prefix);
        if (newCm == null) {
            System.out.println("No content manager with id: " + id + ".");
        } else {
            // Clearing any existing modifications.
            openDirectories.clear();
            openNodes.clear();

            cm = newCm;
            System.out.println("Content manager changed to (" + id +
                    ", " + prefix + ").");
        }
    }

    /**
     * Reads a list of nodes which are contained in a directory
     * with the given path and prints their names.
     * @param path
     * @throws org.jboss.shotoku.exceptions.ResourceDoesNotExist If path is not a directory.
     */
    public void listNodes(String path)
            throws ResourceDoesNotExist {
        NodeList nl = cm.getDirectory(path).getNodes();
        System.out.println("Nodes in directory: " + path);
        for (Node n : nl) {
            System.out.println("  " + n.getName());
        }
    }

    /**
     * Reads a list of directories which are contained in a directory
     * with the given path and prints their names.
     * @param path
     * @throws ResourceDoesNotExist If path is not a directory.
     */
    public void listDirectories(String path)
            throws ResourceDoesNotExist {
        List<Directory> dirs = cm.getDirectory(path).getDirectories();
        System.out.println("Directories in directory: " + path);
        for (Directory d : dirs) {
            System.out.println("  " + d.getName());
        }
    }

    public void listProperties(String path) throws ResourceDoesNotExist {
        Resource res;
        try {
            res = cm.getNode(path);
        } catch (ResourceDoesNotExist resourceDoesNotExist) {
            res = cm.getDirectory(path);
        }

        System.out.println("Properties on path: " + path);
        Map<String, String> props = res.getProperties();
        for (String propName : props.keySet()) {
            System.out.println("   " + propName + " = " + props.get(propName));
        }
    }

    public void setNodeContent(String path, String content)
            throws ResourceAlreadyExists, NameFormatException {
        Node n;
        try {
            n = cm.getNode(path);
        } catch (ResourceDoesNotExist resourceDoesNotExist) {
            n = cm.getRootDirectory().newNode(path);
        }

        n.setContent(content);
        openNodes.add(n);

        System.out.println("Content of node " + path + " changed.");
        System.out.println("No modifications will be done on the repository" +
                " until the modified resources are saved.");
    }

    public void setNodeProprety(String path, String name, String value)
            throws ResourceAlreadyExists, NameFormatException {
        Node n;
        try {
            n = cm.getNode(path);
        } catch (ResourceDoesNotExist resourceDoesNotExist) {
            n = cm.getRootDirectory().newNode(path);
        }

        n.setProperty(name, value);
        openNodes.add(n);

        System.out.println("Property " + name + " on node " + path +
                " changed.");
        System.out.println("No modifications will be done on the repository" +
                " until the modified resources are saved.");
    }

    public void newDirectory(String path)
            throws ResourceAlreadyExists, NameFormatException {
        openDirectories.add(cm.getRootDirectory().newDirectory(path));

        System.out.println("Directory " + path + " created.");
        System.out.println("No modifications will be done on the repository" +
                " until the modified resources are saved.");
    }

    public void saveAll(String logMessage) throws SaveException {
        Set<Resource> toSave = new HashSet<Resource>();
        toSave.addAll(openDirectories);
        toSave.addAll(openNodes);
        cm.save(logMessage, toSave);

        openDirectories.clear();
        openNodes.clear();

        System.out.println("All modified resource saved.");
    }

    public void discardAll() {
        openDirectories.clear();
        openNodes.clear();
        System.out.println("All changes to resources discarded.");
    }

    public void listModifiedResources() {
        System.out.println("Modified nodes:");
        for (Node n : openNodes) {
            System.out.println("  " + n.getName());
        }
        System.out.println();

        System.out.println("Modified directories:");
        for (Directory d : openDirectories) {
            System.out.println("  " + d.getName());
        }
    }

    /*
     * ---
     */

    private static BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

    private static String read(String name) throws IOException {
        System.out.println("Please input " + name + ":");
        return br.readLine();
    }

    public static void main(String[] argv) throws IOException {

        /*System.out.println("Welcome to Shotoku example!");

        ExampleEmbedded ee = new ExampleEmbedded();

        while (true) {
            System.out.println("Choose your action:");
            System.out.println("1. Change ContentManager parameters");
            System.out.println("2. List nodes in a directory");
            System.out.println("3. List directories in a directory");
            System.out.println("4. List resource properties");
            System.out.println("5. Change node content (possible create a new node)");
            System.out.println("6. Change node property (possible create a new node)");
            System.out.println("7. Create new directory");
            System.out.println("8. Save all modified resources");
            System.out.println("9. List all modified resources");
            System.out.println("10. Discard all modified resources");
            System.out.println("11. Exit");

            String choiceStr = br.readLine();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid input.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        ee.changeContentManager(read("id"),
                                read("prefix"));
                        break;

                    case 2:
                        ee.listNodes(read("path"));
                        break;

                    case 3:
                        ee.listDirectories(read("path"));
                        break;

                    case 4:
                        ee.listProperties(read("path"));
                        break;

                    case 5:
                        ee.setNodeContent(read("path"), read("content"));
                        break;

                    case 6:
                        ee.setNodeProprety(read("path"), read("name"),
                                read("value"));
                        break;

                    case 7:
                        ee.newDirectory(read("path"));
                        break;

                    case 8:
                        ee.saveAll(read("log message"));
                        break;

                    case 9:
                        ee.listModifiedResources();
                        break;

                    case 10:
                        ee.discardAll();
                        break;

                    case 11:
                        System.out.println("Bye!");
                        System.exit(0);
                }

                System.out.println("---");
                System.out.println("Press enter to continue.");
                br.readLine();

            } catch (Exception e) {
                System.out.println("Exception " + e.getClass().getName() +
                    " caught, message: " + e.getMessage() + ".");
            }
        }   */
    }
}
