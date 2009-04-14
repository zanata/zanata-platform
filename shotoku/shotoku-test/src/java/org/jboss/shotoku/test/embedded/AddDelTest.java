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

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
* @author Pawel Wrzeszcz (pawel.wrzeszcz@gmail.com)
*/
public class AddDelTest {
	private static final String SHOTOKU_STATUS_DIR = "status";

	private static ContentManager cm = ContentManager.getContentManager("shotoku-test");

    public static void main(String[] args) throws Exception {
        try {
            AddDelTest.save();
        } finally {
            try {
                cm.getRootDirectory().getDirectory(SHOTOKU_STATUS_DIR).delete();
            } catch (ResourceDoesNotExist e) {

            }
        }
    }

    public static boolean save() {
		Directory d = getDir(cm.getRootDirectory(), SHOTOKU_STATUS_DIR);
		try {
			d.delete();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (DeleteException e) {
			e.printStackTrace();
		}

		for (Directory dd : cm.getRootDirectory().getDirectories()) {
            System.out.println("Dir: " + dd.getFullName());
		}

		// Get directory where we store historical scores
		Directory dir = getDir(cm.getRootDirectory(), SHOTOKU_STATUS_DIR);

		if (dir == null) {
            System.out.println("Z");
            return false;
		}

		System.out.println("Directory: " + dir.getFullName());


		Node node = getNode(dir, getCurrentNodeName());

		System.out.println("Node: " + node.getFullName());

		return save(node);
	}

	private static boolean save(Node node) {

		try {
			OutputStream os = node.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(new Serializable() { });
			oos.close();
			node.save("update"); /* os is closed inside save() */
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (RepositoryException e) {
			e.printStackTrace();
			return false;
		} catch (SaveException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private static Node getNode(Directory dir, String nodeName) {

		Node node = null;

		try {
			node = dir.getNode(nodeName);
		} catch (RepositoryException e) {
			System.out.println("Error while getting shotoku node " +
					dir.getFullName() + "/" + nodeName);
            e.printStackTrace();
        } catch (ResourceDoesNotExist e) {
			node = createNode(dir, nodeName);
		}

		return node;
	}

	private static Node createNode(Directory dir, String nodeName) {

		Node node = null;

		try {
			node = dir.newNode(nodeName);
			node.save("create");
		} catch (Exception e) {
			System.out.println("Error while creating shotoku node: " +
					dir.getFullName() + "/" + nodeName);
            e.printStackTrace();
        }

		return node;
	}

	private static Directory getDir(Directory rootDir, String dirName) {

		Directory dir = null;

		try {
			dir = rootDir.getDirectory(dirName);
		} catch (RepositoryException e) {
			System.out.println("Error while getting shotoku directory " +
					rootDir.getFullName() + "/" + dirName);
            e.printStackTrace();
        } catch (ResourceDoesNotExist e) {
			dir = createDir(rootDir, dirName);
		}

		return dir;
	}

	private static Directory createDir(Directory rootDir, String dirName) {

		Directory dir = null;

		try {
			dir = rootDir.newDirectory(dirName);
			dir.save("create");
		} catch (Exception e) {
			System.out.println("Error while creating shotoku directory: " +
					rootDir.getFullName() + "/" + dirName);
            e.printStackTrace();
        }

		return dir;
	}

	private static String getNodeName(Date date) {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		return df.format(date);
	}

	private static String getCurrentNodeName() {

		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();

		return getNodeName(today);
	}

}