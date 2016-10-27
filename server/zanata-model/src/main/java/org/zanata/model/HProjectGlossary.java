package org.zanata.model;

import java.io.Serializable;
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

import org.hibernate.annotations.NaturalId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Table(name = "HProject_Glossary")
@NoArgsConstructor
public class HProjectGlossary implements Serializable {
    private HProjectGlossaryPk id = new HProjectGlossaryPk();

    public HProjectGlossary(Glossary glossary, HProject project) {
        id.setGlossary(glossary);
        id.setProject(project);
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
    @Setter
    @Getter
    @Access(AccessType.FIELD)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HProjectGlossaryPk implements Serializable {

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "glossaryId", nullable = false)
        private Glossary glossary;

        @ManyToOne(fetch = FetchType.EAGER, optional = false)
        @JoinColumn(name = "projectId", nullable = false)
        private HProject project;
    }
}
