package org.zanata.cache;

import com.google.common.cache.CacheLoader;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class EhcacheWrapper<K, V> implements CacheWrapper<K, V>
{
    private final String cacheName;
    private final CacheManager cacheManager;

    public EhcacheWrapper(final String cacheName, final CacheManager cacheManager)
    {
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    @Override
    public void put(final K key, final V value)
    {
        getCache().put(new Element(key, value));
    }

    @Override
    public V get(final K key)
    {
        Element element = getCache().get(key);
        if (element != null)
        {
            return (V) element.getValue();
        }
        return null;
    }

    @Override
    public V getWithLoader(final K key)
    {
       Element element = getCache().getWithLoader(key, null, null);
       if (element != null)
       {
          return (V) element.getValue();
       }
       return null;
    }

    public Ehcache getCache()
    {
        return cacheManager.getEhcache(cacheName);
    }

    public static <K, V> EhcacheWrapper<K, V> create(final String cacheName, final CacheManager cacheManager) 
    {
       cacheManager.addCacheIfAbsent(cacheName);
       return new EhcacheWrapper<K, V>(cacheName, cacheManager);
    }

    public static <K, V> EhcacheWrapper<K, V> create(final String cacheName, final CacheManager cacheManager, CacheLoader<K, V> loader)
    {
       EhcacheWrapper<K, V> cacheWrapper = create(cacheName, cacheManager);
       cacheWrapper.getCache().registerCacheLoader(new EhcacheLoader<K, V>(loader));
       return cacheWrapper;
    }
    
}