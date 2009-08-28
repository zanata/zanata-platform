package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

import org.fedorahosted.flies.validators.Slug;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

	@Entity
	@Table(name="repository")
	public class Repository implements Serializable {

	    private Long id;
	    private String name;
	    private String url;
	    private String path;
	    
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
	    public String getPath() {
	        return path;
	    }
	    
	    public void setPath(String path) {
	        this.path = path;
	    }

	    @Length(max = 80)
	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }
}
