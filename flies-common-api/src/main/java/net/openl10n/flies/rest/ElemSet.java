package net.openl10n.flies.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public abstract class ElemSet<T> implements Collection<T>
{

   private final Set<T> impl;

   protected abstract T valueOfElem(String value);

   public ElemSet(String values)
   {
      impl = new HashSet<T>();
      if (values != null)
      {
         String[] splitValues = StringUtils.split(values, ';');
         for (String val : splitValues)
         {
            T elem = valueOfElem(val);
            add(elem);
         }
      }
   }

   @Override
   public boolean add(T e)
   {
      return impl.add(e);
   }

   @Override
   public boolean addAll(Collection<? extends T> c)
   {
      return impl.addAll(c);
   }

   @Override
   public void clear()
   {
      impl.clear();
   }

   @Override
   public boolean contains(Object o)
   {
      return impl.contains(o);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   {
      return impl.containsAll(c);
   }

   @Override
   public boolean isEmpty()
   {
      return impl.isEmpty();
   }

   @Override
   public Iterator<T> iterator()
   {
      return impl.iterator();
   }

   @Override
   public boolean remove(Object o)
   {
      return impl.remove(o);
   }

   @Override
   public boolean removeAll(Collection<?> c)
   {
      return impl.removeAll(c);
   }

   @Override
   public boolean retainAll(Collection<?> c)
   {
      return impl.retainAll(c);
   }

   @Override
   public int size()
   {
      return impl.size();
   }

   @Override
   public Object[] toArray()
   {
      return impl.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a)
   {
      return impl.toArray(a);
   }

   @Override
   public String toString()
   {
      return StringUtils.join(this, ";");
   }

}
