package org.zanata.model;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class Glossary implements Serializable {

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
}
