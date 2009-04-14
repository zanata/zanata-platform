package org.jboss.shotoku.tags.dal;

import org.jboss.shotoku.tags.AbstractTag;
import org.jboss.shotoku.tags.ShotokuTag;
import org.jboss.shotoku.tags.WebsiteTag;
import org.jboss.shotoku.tags.tools.Constants;

import javax.persistence.*;
import java.util.Date;
import java.io.Serializable;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
@Entity
@Table(name = "ShotokuTags")
@IdClass(IdEntity.class)
public class TagEntity implements Serializable {
    //private static final Logger log = Logger.getLogger(AdministratedService.class);

    private String name;
    private String author;
    private String resourceId;
    private String data;
    private String type;
    private Boolean synced;

    private Date dateCreated;

    public TagEntity() {

    }

    /*
     * Fields.
     */

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Id
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column
    public Boolean isSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    /*
     * Other methods.
     */

    @Transient
    public AbstractTag getTag() {
        if (Constants.SHOTOKU_TAG.equals(getType())) {
            return new ShotokuTag(getName(), getAuthor(), getResourceId(),
                    getData(), getDateCreated());
        } else if (Constants.WEBSITE_TAG.equals(getType())) {
            return new WebsiteTag(getName(), getAuthor(), getResourceId(),
                    getData(), getDateCreated());
        } else {
            return null;
        }
    }

    /**
     * In the given string, replaces all non-alphabetic charaters
     * with underscores.
     */
    @Transient
    private String simplifyString(String s) {
        StringBuffer sb =  new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
                sb.append(c);
            } else {
                if ((i > 0) && (c >= '0') && (c <= '9')) {
                    sb.append(c);
                } else {
                    sb.append('_');
                }
            }
        }

        return sb.toString();
    }

    @Transient
    public String getShotokuPropReprName() {
        return Constants.SHOTOKU_TAG_REPR_PREFIX +
                simplifyString(name) +
                Constants.SHOTOKU_TAG_REPR_SEPARATOR +
                simplifyString(author);
    }

    @Transient
    public String getShotokuPropReprValue() {
        return name + Constants.SHOTOKU_TAG_REPR_SEPARATOR +
                author + Constants.SHOTOKU_TAG_REPR_SEPARATOR +
                dateCreated + Constants.SHOTOKU_TAG_REPR_SEPARATOR +
                data;
    }

    @Transient
    public TagEntity normalizeName() {
        name = name.toLowerCase().trim();
        while (name.contains("  ")) {
            name = name.replace("  ", " ");
        }

        return this;
    }
}
