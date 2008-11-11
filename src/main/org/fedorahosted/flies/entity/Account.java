package org.fedorahosted.flies.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;

import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class Account implements Serializable{

    private Long id;
    private Integer version;
    private String username;
    private String passwordHash;
    private boolean enabled;   
    
    private Person person;
    private Set<AccountRole> roles;

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
    
    @OneToOne(optional=true, fetch=FetchType.EAGER)
    public Person getPerson() {
		return person;
	}
    
    public void setPerson(Person person) {
		this.person = person;
	}
    
    @NotNull @UserPrincipal
    public String getUsername()
    {
       return username;
    }
    
    public void setUsername(String username)
    {
       this.username = username;
    }
    
    @UserPassword(hash = PasswordHash.ALGORITHM_MD5)
    public String getPasswordHash()
    {
       return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash)
    {
       this.passwordHash = passwordHash;      
    }      
    
    @UserEnabled
    public boolean isEnabled()
    {
       return enabled;
    }

    public void setEnabled(boolean enabled)
    {
       this.enabled = enabled;      
    }   

    @UserRoles
    @ManyToMany(targetEntity = AccountRole.class)
    @JoinTable(name = "AccountMembership", 
          joinColumns = @JoinColumn(name = "account_id"),
          inverseJoinColumns = @JoinColumn(name = "member_of")
       )
    public Set<AccountRole> getRoles()
    {
       return roles;
    }
    
    public void setRoles(Set<AccountRole> roles)
    {
       this.roles = roles;
    }
    
}
