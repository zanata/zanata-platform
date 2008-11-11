package org.fedorahosted.flies.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
public class Person {

	private Long id;
    private Integer version;
    private String name;
    private Account account;

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
    
    
    
    
}
