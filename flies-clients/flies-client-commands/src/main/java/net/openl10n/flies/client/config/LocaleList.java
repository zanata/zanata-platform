package net.openl10n.flies.client.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.rest.dto.DTOUtil;

@XmlRootElement(name = "locales")
@XmlType(name = "localesType")
public class LocaleList implements List<Locale>
{
   // private String mapping;

   private final List<Locale> locales = new ArrayList<Locale>();

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
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

   public Iterator<Locale> iterator()
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

   public boolean add(Locale e)
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

   public boolean addAll(Collection<? extends Locale> c)
   {
      return locales.addAll(c);
   }

   public boolean addAll(int index, Collection<? extends Locale> c)
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

   public Locale get(int index)
   {
      return locales.get(index);
   }

   public Locale set(int index, Locale element)
   {
      return locales.set(index, element);
   }

   public void add(int index, Locale element)
   {
      locales.add(index, element);
   }

   public Locale remove(int index)
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

   public ListIterator<Locale> listIterator()
   {
      return locales.listIterator();
   }

   public ListIterator<Locale> listIterator(int index)
   {
      return locales.listIterator(index);
   }

   public List<Locale> subList(int fromIndex, int toIndex)
   {
      return locales.subList(fromIndex, toIndex);
   }

}
