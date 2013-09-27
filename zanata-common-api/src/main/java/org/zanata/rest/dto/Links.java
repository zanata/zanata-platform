package org.zanata.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "linksType", propOrder = {})
public class Links extends ArrayList<Link> {

    private static final long serialVersionUID = 1L;

    /**
     * Retrieve the first found link of with the given type or null of it
     * doesn't exist
     *
     * @param type
     *            attribute of link to search for
     * @return first found Link or null
     */
    public Link findLinkByType(String type) {
        for (Link link : this) {
            if (type.equals(link.getType()))
                return link;
        }
        return null;
    }

    /**
     * Retrieve the first found link of with the given rel or null of it doesn't
     * exist
     *
     * @param rel
     *            attribute of link to search for
     * @return first found Link or null
     */
    public Link findLinkByRel(String rel) {
        for (Link link : this) {
            if (rel.equals(link.getRel()))
                return link;
        }
        return null;
    }

    /**
     * Retrieve all links of with the given type
     *
     * @param type
     *            attribute of link to search for
     * @return List of found Links
     */
    public List<Link> findLinksByType(String type) {
        List<Link> foundLinks = new ArrayList<Link>();
        for (Link link : this) {
            if (type.equals(link.getType()))
                foundLinks.add(link);
        }
        return foundLinks;
    }

    /**
     * Retrieve all links of with the given rel
     *
     * @param rel
     *            attribute of link to search for
     * @return List of found Links
     */
    public List<Link> findLinksByRel(String rel) {
        List<Link> foundLinks = new ArrayList<Link>();
        for (Link link : this) {
            if (rel.equals(link.getRel()))
                foundLinks.add(link);
        }
        return foundLinks;
    }

}
