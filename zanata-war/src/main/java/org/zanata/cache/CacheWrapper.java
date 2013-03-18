package org.zanata.cache;

public interface CacheWrapper<K, V>
{
   void put(K key, V value);
   V get(K key);
   V getWithLoader(K key);
   boolean remove(K key);
}
