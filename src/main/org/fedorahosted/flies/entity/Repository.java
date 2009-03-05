package org.fedorahosted.flies.entity;
import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

	@Entity
	@Table(name="repository")
	public class Repository implements Serializable {

	    private Long id;
	    private String name;
	    private String slug;
	    private String url;
	    
	    @Id @GeneratedValue
	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }
	    
	    @Length(max = 80)
	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }
	    
	    @Length(max = 80)
	    public String getSlug() {
	        return slug;
	    }
	    
	    public void setSlug(String slug) {
	        this.slug = slug;
	    }

	    @Length(max = 80)
	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }
}
