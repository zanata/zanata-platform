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
package org.jboss.shotoku.tools;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;
import java.net.URLDecoder;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.service.ShotokuService;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.NameFormatException;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.activation.MimetypesFileTypeMap;

/**
 * Utility, helper functions used internally.
 *
 * @author Adam Warski (adamw@aster.pl)
 */
public class Tools {
    private static ShotokuService instance;

    public static ShotokuService getService() {
        try {
            synchronized (Tools.class) {
                if (instance == null) {
                    if (ContentManager.isEmbedded()) {
                        // Embedded mode - simply creating a new service instance.
                        instance = (ShotokuService) Thread.currentThread().
                                getContextClassLoader().loadClass(
                                "org.jboss.shotoku.service.ShotokuServiceImpl").newInstance();
                        instance.create();
                    } else {
                        // Application server mode - creating a proxy to an mbean.
                        instance = (ShotokuService) MBeanProxyExt.create(
                                ShotokuService.class,
                                Constants.SHOTOKU_SERVICE_NAME,
                                MBeanServerLocator.locate());
                    }
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads text contained in a tag of the form <tag>text</tag>.
     *
     * @param textNode
     *            Node to read text from.
     * @return Text contained in the given node.
     */
    public static String unmarshallText(org.w3c.dom.Node textNode) {
        StringBuffer buf = new StringBuffer();

        org.w3c.dom.Node n;
        org.w3c.dom.NodeList nodes = textNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                buf.append(n.getNodeValue());
            }
        }

        return buf.toString();
    }

    /**
     * @param root Node which children should be searched.
     * @param name Name of the node that is being searched.
     * @return A child node of the given one with the given name if such
     * exists, or null otherwise.
     */
    public static org.w3c.dom.Node getNamedNode(org.w3c.dom.Node root,
                                                String name) {
        NodeList nl = root.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node n = nl.item(i);
            if (name.equals(n.getNodeName())) {
                return n;
            }
        }

        return null;
    }

    /**
     * From the given node, gets the value of the specified attribute.
     *
     * @param root
     *            Node to read the attribute from.
     * @param attrName
     *            Name of the attribute to read.
     * @return Value of the given attribute or null, if no such attribute is
     *         present.
     */
    public static String getAttributeValue(org.w3c.dom.Node root,
                                           String attrName) {
        org.w3c.dom.Node n = root.getAttributes().getNamedItem(attrName);

        if (n == null)
            return null;

        return unmarshallText(n);
    }

    /**
     * Reads all attributes of the given node and returns them as a map. Only
     * signle-value attributes are supported.
     * @param root Node from which to read the attributes.
     * @return A map holding all attributes of the given node.
     */
    public static Map<String, String> getMapFromNodeAttributes(Node root) {
        Map<String, String> properties = new HashMap<String, String>();
        NamedNodeMap nnm = root.getAttributes();

        if (nnm != null) {
            Node n;

            for (int i=0; i<nnm.getLength(); i++) {
                n = nnm.item(i);
                if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                    properties.put(n.getNodeName(), Tools.unmarshallText(n));
                }
            }
        }

        return properties;
    }

    /**
     * From the given string, removes unnecessary / in the middle and
     * ending. If startingSlash = true, ensures that the returned string
     * starts with a slash.
     * For example: "///a//b//c/" is transformed to "/a/b/c" (here,
     * startingSlash = true).
     *
     * @param toNormalize
     *            String to normalize.
     * @param startingSlash True if the returned string should have a / in
     * the beginning.
     * @return <code>toNormalize</code> with unnecessary / removed.
     */
    public static String normalizeSlashes(String toNormalize, boolean startingSlash) {
        toNormalize = toNormalize.trim();

        while (toNormalize.contains("//")) {
            toNormalize = toNormalize.replace("//", "/");
        }

        if ((startingSlash) && (!toNormalize.startsWith("/"))) {
            toNormalize = "/" + toNormalize;
        } else if ((!startingSlash) && (toNormalize.startsWith("/"))) {
            toNormalize = toNormalize.substring(1);
        }

        if ((toNormalize.endsWith("/")) && (toNormalize.length() > 1)) {
            toNormalize = toNormalize.substring(0, toNormalize.length() - 1);
        }

        return toNormalize;
    }

    /**
     * Concatenates two parts of a path into 1: returns a string
     * <code>path1/path2</code>, only with the necessary slashes
     * (so running normalizeSlashes(String) won't have any effect
     * on the string).
     *
     * @param path1
     *            First path to concatenate.
     * @param path2
     *            Second path to concatenate.
     * @return <code>path1/path2</code>
     */
    public static String concatenatePaths(String path1, String path2) {
        if ("".equals(path1) || "".equals(path2)) {
            return path1 + path2;
        }

        boolean p1e = path1.endsWith("/");
        boolean p2s = path2.startsWith("/");

        if (p1e && p2s) {
            return path1 + path2.substring(1);
        }

        if (p1e || p2s) {
            return path1 + path2;
        }

        return path1 + "/" + path2;
    }

    /**
     * Concatenates two parts of a property name: returns a string
     * <code>property1.property2</code>.
     *
     * @param property1
     *            First property part to concatenate.
     * @param property2
     *            Second property part to concatenate.
     * @return <code>property1.property2</code>
     */
    public static String concatenateProperties(String property1,
                                               String property2) {
        return property1 + "." + property2;
    }

    /**
     * Concatenates two parts of a path into 1: returns a string
     * <code>path1/path2</code>.
     * @param path1
     *            First path to concatenate.
     * @param path2
     *            Second path to concatenate.
     * @return <code>path1/path2</code>
     */
    public static String addPaths(String path1, String path2) {
        return path1 + "/" + path2;
    }

    /**
     * Checks if the given string is empty (null or "").
     * @param s String to check.
     * @return True iff the given string is null or equal to "".
     */
    public static boolean isEmpty(String s) {
        return (s == null) || ("".equals(s));
    }

    /**
     * Converts the given object to a String in a null-safe way.
     * @param o Object to convert.
     * @return Result of o.toString() or null if o is null.
     */
    public static String toString(Object o) {
        if (o == null) {
            return null;
        }

        return o.toString();
    }

    /**
     * Converts the given object to a String in a null-safe way.
     * Never returns a null.
     * @param o Object to convert.
     * @return Result of o.toString() or an emptys tring if o is null.
     */
    public static String toStringNotNull(Object o) {
        if (o == null) {
            return "";
        }

        return o.toString();
    }

    /**
     * Encodes the given string to an URL-friendly format (calls
     * URLEncoder.encodeURL).
     * @param s String to encodeURL.
     * @return Encoded string.
     */
    public static String encodeURL(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes the given string from aa URL-friendly format (calls
     * URLDecoder.decodeURL).
     * @param s String to decodeURL.
     * @return Decoded string.
     */
    public static String decodeURL(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if two objects are equal - either both null, or
     * their equals method returns true.
     * @param obj1 First object to compare.
     * @param obj2 Second object to compare.
     * @return True iff both objects are equal to null or if
     * their equals method returns true.
     */
    public static boolean objectsEqual(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) ||
                ((obj1 != null) &&  (obj1.equals(obj2)));
    }

    /**
     * Checks if the link is an outside link.
     * @param link Link to check.
     * @return True iff the given link is an outside link.
     */
    public static boolean isOutsideLink(String link) {
        if (link == null) {
            return false;
        }

        link = link.trim();
        return link.startsWith("http:") || link.startsWith("ftp:")
                || link.startsWith("https:") || link.startsWith("mailto:")
                || link.startsWith("news:") || link.startsWith("file:");
    }

    /**
     * Transferes all bytes from the given input stream to the given output
     * stream.
     *
     * @param is
     *            Input stream to read from.
     * @param os
     *            Output stream to write to.
     * @throws IOException In case of an IO exception.
     */
    public static void transfer(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[ContentManager.getTransferBufferSize()];
        int read;
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
    }

    /**
     * Transferes all bytes from the given input stream to the given output
     * stream.
     *
     * @param is
     *            Input stream to read from.
     * @param w
     *            Printwriter to write to.
     * @throws IOException In case of an IO exception.
     */
    public static void transfer(InputStream is, PrintWriter w) throws IOException {
        char[] buffer = new char[ContentManager.getTransferBufferSize()];
        int read;
        InputStreamReader isr = new InputStreamReader(is);
        while ((read = isr.read(buffer)) != -1) {
            w.write(buffer, 0, read);
        }
    }

    /**
     * Checks if the given string represents a true value. For example, "true",
     * "yes", "TrUe", "1" etc will return true.
     *
     * @param s
     *            String to check.
     * @return True iff the given string can be determined to represent "true".
     */
    public static boolean isTrue(String s) {
        if (s == null) {
            return false;
        }

        s = s.trim().toLowerCase();
        return "true".equals(s) || "yes".equals(s) || "1".equals(s);
    }

    public static void checkName(String name) throws NameFormatException {
        if (name.contains(" ")) {
            throw new NameFormatException("A resource name cannot cointain a space.");
        }
    }

    /*
     * TEMPORARY FILES
     */

    private static final String TEMP_FILE_PREFIX = "shotoku-base";
    private static final Random random;

    static {
        random = new Random();
    }

    /**
     * @param file File to read from.
     * @return Content of the given file represented as a byte buffer.
     * @throws IOException In case of an IO exception.
     */
    public static ByteBuffer getFileBytes(File file) throws IOException {
        FileChannel fc = new FileInputStream(file).getChannel();
        ByteBuffer buff = ByteBuffer.allocate((int) file.length());
        fc.read(buff);
        buff.flip();

        return buff;
    }

    /**
     * @param file File to read from.
     * @return Content of the given file represented as a String.
     * @throws IOException In case of an IO exception.
     */
    public static String getFileString(File file) throws IOException {
        return Charset.forName(
                System.getProperty("file.encoding")).decode(
                getFileBytes(file)).toString();
    }

    /**
     * @return A new temporary file.
     */
    public static File createTemporaryFile() {
        try {
            return File.createTempFile(TEMP_FILE_PREFIX, Integer.toString(
                    random.nextInt()));
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    /*
     * MIME TYPES
     */

    private static MimetypesFileTypeMap mimeTypes;

    static {
        mimeTypes = new MimetypesFileTypeMap(
                Tools.class.getResourceAsStream("/mime-types.txt"));
    }

    public static String getNameBasedMimeType(String name) {
        return mimeTypes.getContentType(name.toLowerCase());
    }

    public static String getNameBasedMimeType(File file) {
        return mimeTypes.getContentType(file);
    }
}
