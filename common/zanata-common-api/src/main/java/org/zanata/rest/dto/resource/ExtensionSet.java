package org.zanata.rest.dto.resource;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.ExtensionValue;


@XmlRootElement(name = "extension-set")
public class ExtensionSet<T extends ExtensionValue> extends HashSet<T>
{

   private static final long serialVersionUID = 1L;

   public <E extends T> E findByType(Class<E> clz)
   {
      for (T e : this)
      {
         if (clz.isInstance(e))
            return (E) clz.cast(e);
      }
      return null;
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

}
