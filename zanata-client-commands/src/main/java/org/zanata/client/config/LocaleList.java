package org.zanata.client.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.zanata.rest.dto.DTOUtil;


@XmlRootElement(name = "locales")
@XmlType(name = "localesType")
public class LocaleList implements List<LocaleMapping>
{
   // private String mapping;

   private final List<LocaleMapping> locales = new ArrayList<LocaleMapping>();

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   /**
    * WARNING: slow!
    * 
    * @param localLocale
    * @return
    */
   public String getCanonicalLocale(String localLocale)
   {
      for (LocaleMapping loc : locales)
      {
         if (loc.getLocalLocale().equals(localLocale))
            return loc.getLocale();
      }
      return null;
   }

   /**
    * WARNING: slow!
    * 
    * @param canonicalLocale
    * @return
    */
   public String getLocalLocale(String canonicalLocale)
   {
      for (LocaleMapping loc : locales)
      {
         if (loc.getLocale().equals(canonicalLocale))
            return loc.getLocalLocale();
      }
      return null;
   }

   public int size()
   {
      return locales.size();
   }

   public boolean isEmpty()
   {
      return locales.isEmpty();
   }

   public boolean contains(Object o)
   {
      return locales.contains(o);
   }

   public Iterator<LocaleMapping> iterator()
   {
      return locales.iterator();
   }

   public Object[] toArray()
   {
      return locales.toArray();
   }

   public <T> T[] toArray(T[] a)
   {
      return locales.toArray(a);
   }

   public boolean add(LocaleMapping e)
   {
      return locales.add(e);
   }

   public boolean remove(Object o)
   {
      return locales.remove(o);
   }

   public boolean containsAll(Collection<?> c)
   {
      return locales.containsAll(c);
   }

   public boolean addAll(Collection<? extends LocaleMapping> c)
   {
      return locales.addAll(c);
   }

   public boolean addAll(int index, Collection<? extends LocaleMapping> c)
   {
      return locales.addAll(index, c);
   }

   public boolean removeAll(Collection<?> c)
   {
      return locales.removeAll(c);
   }

   public boolean retainAll(Collection<?> c)
   {
      return locales.retainAll(c);
   }

   public void clear()
   {
      locales.clear();
   }

   public boolean equals(Object o)
   {
      return locales.equals(o);
   }

   public int hashCode()
   {
      return locales.hashCode();
   }

   public LocaleMapping get(int index)
   {
      return locales.get(index);
   }

   public LocaleMapping set(int index, LocaleMapping element)
   {
      return locales.set(index, element);
   }

   public void add(int index, LocaleMapping element)
   {
      locales.add(index, element);
   }

   public LocaleMapping remove(int index)
   {
      return locales.remove(index);
   }

   public int indexOf(Object o)
   {
      return locales.indexOf(o);
   }

   public int lastIndexOf(Object o)
   {
      return locales.lastIndexOf(o);
   }

   public ListIterator<LocaleMapping> listIterator()
   {
      return locales.listIterator();
   }

   public ListIterator<LocaleMapping> listIterator(int index)
   {
      return locales.listIterator(index);
   }

   public List<LocaleMapping> subList(int fromIndex, int toIndex)
   {
      return locales.subList(fromIndex, toIndex);
   }

}
