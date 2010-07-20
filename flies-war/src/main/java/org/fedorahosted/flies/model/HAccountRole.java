package org.fedorahosted.flies.model;

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
public class HAccountRole implements Serializable
{
   private static final long serialVersionUID = 9177366120789064801L;

   private Integer id;
   private String name;
   private boolean conditional;

   private Set<HAccountRole> groups;

   @Id
   @GeneratedValue
   public Integer getId()
   {
      return id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   @RoleName
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @RoleGroups
   @ManyToMany(targetEntity = HAccountRole.class)
   @JoinTable(name = "HAccountRoleGroup", joinColumns = @JoinColumn(name = "roleId"), inverseJoinColumns = @JoinColumn(name = "memberOf"))
   public Set<HAccountRole> getGroups()
   {
      return groups;
   }

   public void setGroups(Set<HAccountRole> groups)
   {
      this.groups = groups;
   }

   @RoleConditional
   public boolean isConditional()
   {
      return conditional;
   }

   public void setConditional(boolean conditional)
   {
      this.conditional = conditional;
   }
}
