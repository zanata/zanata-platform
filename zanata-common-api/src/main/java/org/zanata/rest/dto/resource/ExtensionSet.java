package org.zanata.rest.dto.resource;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.ExtensionValue;


@XmlRootElement(name = "extension-set")
public class ExtensionSet<T extends ExtensionValue> extends AbstractCollection<T>
{

   private Map<Class<?>, T> extensions = new LinkedHashMap<Class<?>, T>();
   
   @Override
   public Iterator<T> iterator()
   {
      return extensions.values().iterator();
   }
   
   @Override
   public int size()
   {
      return extensions.size();
   }
   
   @Override
   public boolean add(T e) 
   {
      this.extensions.put(e.getClass(), e);
      return true;
   };
   
   @SuppressWarnings("unchecked")
   public <E extends T> E findByType(Class<E> clz)
   {
      return (E)this.extensions.get(clz);
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   public <E extends T> E findOrAddByType(Class<E> clz)
   {
      E ext = findByType(clz);
      if (ext == null)
      {
         try
         {
            ext = clz.newInstance();
            add(ext);
         }
         catch (Throwable e)
         {
            throw new RuntimeException("unable to create instance", e);
         }
      }
      return ext;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if( obj == null )
      {
         return false;
      }
      else if( !(obj instanceof ExtensionSet) )
      {
         return false;
      }
      else
      {
         @SuppressWarnings("rawtypes")
         ExtensionSet other = (ExtensionSet)obj;
         
         return new EqualsBuilder()
                    .append(this.extensions, other.extensions)
                    .isEquals();
      }
   }
   
   @Override
   public int hashCode()
   {
      HashCodeBuilder hcBuilder = new HashCodeBuilder(15, 67);
      for( T t : this )
      {
         hcBuilder.append(t);
      }
      return hcBuilder.toHashCode();
   }

}
