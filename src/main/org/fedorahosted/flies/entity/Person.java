package org.fedorahosted.flies.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "personId"))
public class Person {

	private Long id;
    private Integer version;
    private String name;
    private Account account;

    private String email;
    
    private String personId;
    
    // from Damned Lies:
    private String imageUrl;
    private String ircNick;
    private String webpageUrl;
    
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

    @NotNull
    @NotEmpty
    public String getPersonId() {
		return personId;
	}
    
    public void setPersonId(String personId) {
		this.personId = personId;
	}
    
    @OneToOne(optional=true, fetch=FetchType.EAGER)
    public Account getAccount() {
		return account;
	}
    
    public void setAccount(Account account) {
		this.account = account;
	}
    
    @Transient
    public boolean hasAccount(){
    	return account != null;
    }
    
    
    public String getImageUrl() {
		return imageUrl;
	}
    
    public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
    
    public String getIrcNick() {
		return ircNick;
	}
    
    public void setIrcNick(String ircNick) {
		this.ircNick = ircNick;
	}
    
    public String getWebpageUrl() {
		return webpageUrl;
	}
    
    public void setWebpageUrl(String webpageUrl) {
		this.webpageUrl = webpageUrl;
	}
    
    public void setEmail(String email) {
		this.email = email;
	}
    
    @Email
    public String getEmail() {
		return email;
	}
    
}
