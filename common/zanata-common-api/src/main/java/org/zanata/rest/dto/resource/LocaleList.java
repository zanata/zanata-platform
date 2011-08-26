/*
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.dto.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.zanata.rest.dto.DTOUtil;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@XmlRootElement(name = "locales")
@XmlType(name = "localesType")
public class LocaleList implements List<String>
{
   private final List<String> locales = new ArrayList<String>();

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int size()
   {
      return locales.size();
   }

   @Override
   public boolean isEmpty()
   {
      return locales.isEmpty();
   }

   @Override
   public boolean contains(Object o)
   {
      return locales.contains(o);
   }

   @Override
   public Iterator<String> iterator()
   {
      return locales.iterator();
   }

   @Override
   public Object[] toArray()
   {
      return locales.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a)
   {
      return locales.toArray(a);
   }

   @Override
   public boolean add(String e)
   {
      return locales.add(e);
   }

   @Override
   public boolean remove(Object o)
   {
      return locales.remove(o);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   {
      return locales.containsAll(c);
   }

   @Override
   public boolean addAll(Collection<? extends String> c)
   {
      return locales.addAll(c);
   }

   @Override
   public boolean addAll(int index, Collection<? extends String> c)
   {
      return locales.addAll(index, c);
   }

   @Override
   public boolean removeAll(Collection<?> c)
   {
      return locales.removeAll(c);
   }

   @Override
   public boolean retainAll(Collection<?> c)
   {
      return locales.retainAll(c);
   }

   @Override
   public void clear()
   {
      locales.clear();
   }

   @Override
   public String get(int index)
   {
      return locales.get(index);
   }

   @Override
   public String set(int index, String element)
   {
      return locales.set(index, element);
   }

   @Override
   public void add(int index, String element)
   {
      locales.add(index, element);
   }

   @Override
   public String remove(int index)
   {
      return locales.remove(index);
   }

   @Override
   public int indexOf(Object o)
   {
      return locales.indexOf(o);
   }

   @Override
   public int lastIndexOf(Object o)
   {
      return locales.lastIndexOf(o);
   }

   @Override
   public ListIterator<String> listIterator()
   {
      return locales.listIterator();
   }

   @Override
   public ListIterator<String> listIterator(int index)
   {
      return locales.listIterator(index);
   }

   @Override
   public List<String> subList(int fromIndex, int toIndex)
   {
      return locales.subList(fromIndex, toIndex);
   }

   @Override
   public int hashCode()
   {
      return locales.hashCode();
   }

   public boolean equals(Object o)
   {
      return locales.equals(o);
   }
}


 