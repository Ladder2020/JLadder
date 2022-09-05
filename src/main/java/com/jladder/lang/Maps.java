package com.jladder.lang;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Maps {
    public static <V, K> Map<V,K> inverse(Map<K,V> map) {
        final Map<V, K> result = createMap(map.getClass());
        map.forEach((key, value) -> result.put(value, key));
        return result;
    }
    public static <K, V> Map<K, V> createMap(Class<?> mapType) {
        if (mapType.isAssignableFrom(AbstractMap.class)) {
            return new HashMap<>();
        } else {
            try {
                return (Map<K, V>) Refs.newInstance(mapType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
