package org.zanata.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Table(name = "HProject_Glossary")
public class HProjectGlossary implements Serializable {
    private HProjectGlossaryPk id = new HProjectGlossaryPk();

    public HProjectGlossary(Glossary glossary, HProject project) {
        id.setGlossary(glossary);
        id.setProject(project);
    }

    public HProjectGlossary() {
    }

    @EmbeddedId
    protected HProjectGlossaryPk getId() {
        return id;
    }

    protected void setId(HProjectGlossaryPk id) {
        this.id = id;
    }

    @Transient
    public HProject getProject() {
        return id.getProject();
    }

    @Transient
    public Glossary getGlossary() {
        return id.getGlossary();
    }

    @Embeddable
    @Access(AccessType.FIELD)
    public static class HProjectGlossaryPk implements Serializable {

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "glossaryId", nullable = false)
        private Glossary glossary;

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "projectId", nullable = false)
        private HProject project;

        @java.beans.ConstructorProperties({ "glossary", "project" })
        public HProjectGlossaryPk(Glossary glossary, HProject project) {
            this.glossary = glossary;
            this.project = project;
        }

        public HProjectGlossaryPk() {
        }

        public Glossary getGlossary() {
            return this.glossary;
        }

        public HProject getProject() {
            return this.project;
        }

        public void setGlossary(Glossary glossary) {
            this.glossary = glossary;
        }

        public void setProject(HProject project) {
            this.project = project;
        }
    }
}
