package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.EntityStatus;
import org.zanata.common.Namespaces;
import org.zanata.common.ProjectType;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.MediaTypes.Format;

/**
 * Representation of the data within a Project resource
 *
 * @author asgeirf
 *
 */
@XmlType(name = "projectType", propOrder = { "name", "defaultType",
        "description", "sourceViewURL", "sourceCheckoutURL", "links",
        "iterations", "status" })
@XmlRootElement(name = "project")
@JsonPropertyOrder({ "id", "defaultType", "name", "description",
        "sourceViewURL", "sourceCheckoutURL", "links", "iterations", "status" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = Inclusion.NON_NULL)
@Label("Project")
public class Project implements Serializable, HasCollectionSample<Project>,
        HasMediaType {

    private static final long serialVersionUID = 7809475317072925948L;
    private String id;
    private String name;
    private String defaultType;
    private String description;
    private String sourceViewURL;
    private String sourceCheckoutURL;
    private EntityStatus status = EntityStatus.ACTIVE;

    private Links links;

    private List<ProjectIteration> iterations;

    public Project() {
    }

    public Project(String id, String name, String defaultType) {
        this.id = id;
        this.name = name;
        this.defaultType = defaultType;
    }

    public Project(String id, String name, String type, String description) {
        this(id, name, type);
        this.description = description;
    }

    /**
     * The project identifier (slug)
     */
    @XmlAttribute(name = "id", required = true)
    @DocumentationExample("my-project")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The project type.
     */
    @XmlElement(name = "defaultType", required = true, nillable = false,
            namespace = Namespaces.ZANATA_OLD)
    @DocumentationExample("Gettext")
    public String getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    @NotEmpty
    @Size(max = 80)
    @XmlElement(name = "name", required = true,
            namespace = Namespaces.ZANATA_OLD)
    @DocumentationExample("My Project")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Size(max = 100)
    @XmlElement(name = "description", required = false,
            namespace = Namespaces.ZANATA_OLD)
    @DocumentationExample("This is a sample project.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The url to view the project's sources.
     */
    @XmlElement(name = "sourceViewURL", required = false, nillable = true,
            namespace = Namespaces.ZANATA_API)
    @DocumentationExample("http://source.view.com")
    public String getSourceViewURL() {
        return sourceViewURL;
    }

    public void setSourceViewURL(String sourceViewURL) {
        this.sourceViewURL = sourceViewURL;
    }

    /**
     * The url where to checkout the project's sources.
     */
    @XmlElement(name = "sourceCheckoutURL", required = false, nillable = true,
            namespace = Namespaces.ZANATA_API)
    @DocumentationExample("http://source.checkout.com")
    public String getSourceCheckoutURL() {
        return sourceCheckoutURL;
    }

    public void setSourceCheckoutURL(String sourceCheckoutURL) {
        this.sourceCheckoutURL = sourceCheckoutURL;
    }

    /**
     * Set of links managed by this project
     *
     * This field is ignored in PUT/POST operations
     *
     * @return set of Links managed by this resource
     */
    @XmlElement(name = "link", namespace = Namespaces.ZANATA_API)
    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonIgnore
    public Links getLinks(boolean createIfNull) {
        if (createIfNull && links == null)
            links = new Links();
        return links;
    }

    /**
     * A list of versions (iterations) in the project
     */
    @XmlElementWrapper(name = "project-iterations",
            namespace = Namespaces.ZANATA_OLD)
    @XmlElementRef(namespace = Namespaces.ZANATA_OLD)
    @JsonProperty("iterations")
    public List<ProjectIteration> getIterations() {
        return iterations;
    }

    public void setIterations(List<ProjectIteration> iterations) {
        this.iterations = iterations;
    }

    public List<ProjectIteration> getIterations(boolean createIfNull) {
        if (createIfNull && iterations == null)
            iterations = new ArrayList<ProjectIteration>();
        return getIterations();
    }

    /**
     * System state of the project
     */
    @XmlElement(name = "status", required = false,
            namespace = Namespaces.ZANATA_OLD)
    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    @Override
    public Project createSample() {
        Project entity = new Project();
        entity.setId("sample-project");
        entity.setName("Sample Project");
        entity.setDescription("Sample Project Description");
        entity.setDefaultType(ProjectType.Gettext.toString());
        entity.getIterations(true).addAll(
                new ProjectIteration().createSamples());
        return entity;
    }

    @Override
    public Collection<Project> createSamples() {
        Collection<Project> entities = new ArrayList<Project>();
        entities.add(createSample());
        Project p2 = createSample();
        p2.setId("another-project");
        p2.setName("Another Sample Project");
        p2.setDescription("Another Sample Project Description");
        p2.setDefaultType(ProjectType.Gettext.toString());
        entities.add(p2);
        return entities;
    }

    public static void main(String args[]) {
        Project test = new Project();
        System.out.println(test.createSample().toString());
    }

    @Override
    public String getMediaType(Format format) {
        return MediaTypes.APPLICATION_ZANATA_PROJECT + format;
    }

    @Override
    public String toString() {
        return DTOUtil.toXML(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result =
                prime * result
                        + ((iterations == null) ? 0 : iterations.hashCode());
        result = prime * result + ((links == null) ? 0 : links.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result =
                prime * result
                        + ((defaultType == null) ? 0 : defaultType.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Project)) {
            return false;
        }
        Project other = (Project) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (iterations == null) {
            if (other.iterations != null) {
                return false;
            }
        } else if (!iterations.equals(other.iterations)) {
            return false;
        }
        if (links == null) {
            if (other.links != null) {
                return false;
            }
        } else if (!links.equals(other.links)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!defaultType.equals(other.defaultType)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        return true;
    }

}
