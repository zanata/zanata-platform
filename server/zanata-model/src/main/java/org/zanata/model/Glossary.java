package org.zanata.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@Table(uniqueConstraints = @UniqueConstraint(name = "Idx_qualifiedName",
        columnNames = "qualifiedName"))
public class Glossary implements Serializable {

    private static final long serialVersionUID = 1L;

    public Glossary(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique glossary name
     *
     * e.g. project/{project slug}, global/default
     */
    @NotNull
    @Field(analyze = Analyze.NO)
    private String qualifiedName;

    public Glossary() {
    }

    public Long getId() {
        return this.id;
    }

    /**
     * Unique glossary name
     *
     * e.g. project/{project slug}, global/default
     */
    public String getQualifiedName() {
        return this.qualifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Glossary glossary = (Glossary) o;
        return Objects.equals(qualifiedName, glossary.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
}
