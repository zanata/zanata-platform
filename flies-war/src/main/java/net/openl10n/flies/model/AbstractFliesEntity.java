package net.openl10n.flies.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@MappedSuperclass
public class AbstractFliesEntity
{

   protected Long id;
   protected Date creationDate;
   protected Date lastChanged;

   protected Integer versionNum;

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   @Version
   @Column(nullable = false)
   public Integer getVersionNum()
   {
      return versionNum;
   }

   public void setVersionNum(Integer versionNum)
   {
      this.versionNum = versionNum;
   }

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   public Date getCreationDate()
   {
      return creationDate;
   }

   public void setCreationDate(Date creationDate)
   {
      this.creationDate = creationDate;
   }

   @Temporal(TemporalType.TIMESTAMP)
   @Column(nullable = false)
   public Date getLastChanged()
   {
      return lastChanged;
   }

   public void setLastChanged(Date lastChanged)
   {
      this.lastChanged = lastChanged;
   }

   @PrePersist
   private void onPersist()
   {
      creationDate = new Date();
      lastChanged = creationDate;
   }

   @PreUpdate
   private void onUpdate()
   {
      lastChanged = new Date();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((lastChanged == null) ? 0 : lastChanged.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      AbstractFliesEntity other = (AbstractFliesEntity) obj;
      if (creationDate == null)
      {
         if (other.creationDate != null)
            return false;
      }
      else if (!creationDate.equals(other.creationDate))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (lastChanged == null)
      {
         if (other.lastChanged != null)
            return false;
      }
      else if (!lastChanged.equals(other.lastChanged))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "[id=" + id + ",versionNum=" + versionNum + "]";
   }

}
