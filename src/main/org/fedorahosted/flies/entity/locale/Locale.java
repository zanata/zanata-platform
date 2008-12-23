package org.fedorahosted.flies.entity.locale;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Locale implements Serializable{

	private Long id;
    private Integer version;
    private String name;

    private String countryCode;
    private String languageCode;
    private String qualifier;
    private String variant;
    
    private boolean leftToRight;
    
    private Locale parent;
    private List<Locale> children;
    
    private List<Locale> friends; // e.g. nn, nb. 
    
    
    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
		return name;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
}
