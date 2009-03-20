package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.jboss.seam.annotations.security.management.RoleConditional;
import org.jboss.seam.annotations.security.management.RoleGroups;
import org.jboss.seam.annotations.security.management.RoleName;

@Entity
public class AccountRole implements Serializable {
	private static final long serialVersionUID = 9177366120789064801L;

	private Integer id;
	private String name;
	private boolean conditional;

	private Set<AccountRole> groups;

	@Id
	@GeneratedValue
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@RoleName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@RoleGroups
	@ManyToMany(targetEntity = AccountRole.class)
	@JoinTable(name = "AccountRoleGroup", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "member_of"))
	public Set<AccountRole> getGroups() {
		return groups;
	}

	public void setGroups(Set<AccountRole> groups) {
		this.groups = groups;
	}

	@RoleConditional
	public boolean isConditional() {
		return conditional;
	}

	public void setConditional(boolean conditional) {
		this.conditional = conditional;
	}
}
