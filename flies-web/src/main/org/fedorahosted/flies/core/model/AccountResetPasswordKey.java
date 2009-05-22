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
public class AccountResetPasswordKey extends AbstractAccountKey implements Serializable{

	private static final long serialVersionUID = 1L;
	
}
