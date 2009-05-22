package org.fedorahosted.flies.core.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.security.management.PasswordHash;

@Entity
public class AccountActivationKey implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String keyHash;
	private Account account;

	@NotEmpty
	@Length(max=64)
	@Id
	public String getKeyHash() {
		return keyHash;
	}
	
	public void setKeyHash(String keyHash) {
		this.keyHash = keyHash;
	}
	
	@NaturalId
	@OneToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name = "accountId")
	public Account getAccount() {
		return account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
