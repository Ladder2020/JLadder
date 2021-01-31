package com.jladder.data;

public class KeyValue<K,V> {
    public K key;
    public V value;

    public boolean IsNull()
    {
        return value != null;
    }
    public KeyValue() { }

    public KeyValue(K key, V value)
    {
        if (key == null) return;
        this.key = key;
        this.value = value;
    }
}
