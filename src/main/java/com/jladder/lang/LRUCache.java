package com.jladder.lang;

import java.util.*;

public class LRUCache<K, V> {
    private static final float hashTableLoadFactor = 0.75f;
    private LinkedHashMap<K, V> map;
    private int cacheSize;

    /***
     * 构造
     * @param cacheSize 缓存大小
     */
    public LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
        map = new LinkedHashMap<K, V>(hashTableCapacity, hashTableLoadFactor,true) {
            // (an anonymous inner class)
            private static final long serialVersionUID = 1;
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.cacheSize;
            }
        };
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized void remove(String key){
        map.remove(key);
    }

    public synchronized int usedEntries() {
        return map.size();
    }
    public synchronized Collection<Map.Entry<K, V>> getAll() {
        return new ArrayList<Map.Entry<K, V>>(map.entrySet());
    }

    public synchronized List<V> getValues() {
        return new ArrayList<V>(map.values());
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
