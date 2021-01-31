package com.jladder.hub;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jladder.datamodel.DataModelForMapRaw;
import com.jladder.lang.func.Func1;
import com.jladder.proxy.ProxyConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WorkCache implements IWorkCache{
    private final int dmtime=5;
    private final int proxytime=5;
    private final int moduletime=20;

    private Cache<String, DataModelForMapRaw> dmcache = Caffeine.newBuilder().maximumSize(200).expireAfterAccess(dmtime, TimeUnit.MINUTES).build();

    private Cache<String, ProxyConfig> proxycache = Caffeine.newBuilder().maximumSize(200).expireAfterAccess(proxytime, TimeUnit.MINUTES).build();

    private Map<String,Cache<String,Object>> timedCache = new ConcurrentHashMap<String,Cache<String,Object>>();

    public WorkCache(){
//         Cache<String, DataModelForMapRaw> dd = Caffeine.newBuilder().maximumSize(10_000)
//                .expireAfterWrite(5, TimeUnit.MINUTES)
//                .refreshAfterWrite(1, TimeUnit.MINUTES).build();
    }

//    TimedCache<String, Object> timedCache = new TimedCache<String, Object>(3600);
    @Override
    public void addDataModelCache(String key, DataModelForMapRaw raw) {
        dmcache.put("_DataModel_"+key,raw);
    }

    @Override
    public DataModelForMapRaw getDataModelCache(String key) {
        return dmcache.getIfPresent("_DataModel_"+key);
    }

    @Override
    public void removeDataModelCache(String key) {
        dmcache.invalidate("_DataModel_"+key);
    }

    @Override
    public void removeAllDataModelCache() {
        dmcache.invalidateAll();
    }

    @Override
    public void addProxyCache(String key, ProxyConfig raw) {
        proxycache.put(key,raw);
    }

    @Override
    public ProxyConfig getProxyCache(String key) {
        return proxycache.getIfPresent(key);
    }

    @Override
    public void removeProxyCache(String key) {
        proxycache.invalidate(key);
    }

    @Override
    public void removeAllProxyCache() {
        proxycache.invalidateAll();
    }

    @Override
    public void addLatchDataCache(String key, String cryptosql, Object data, int stayTime) {


    }

    @Override
    public void addLatchDependsCache(String key, List<String> names) {

    }

    @Override
    public List<String> getLatchDependsCache(String key) {
        return null;
    }

    @Override
    public Object getLatchDataCache(String key, String cryptosql) {
        return null;
    }

    @Override
    public void removeLatchDataCache(String key, String cryptosql) {

    }

    @Override
    public <T> void addModuleCache(String key, T data, String module) {
        if(timedCache.containsKey(module)){
            timedCache.get(module).put(key,data);
        }else{
            Cache<String,Object> cache = Caffeine.newBuilder().maximumSize(200).expireAfterAccess(moduletime, TimeUnit.MINUTES).build();
            timedCache.put(module,cache);
            cache.put(key,data);
        }
    }

    @Override
    public <T> void addModuleCache(String key, T data, String module, int second) {
        if(timedCache.containsKey(module)){
            Cache<String, Object> cache = timedCache.get(module);
            cache.put(key,data);
        }else{
            Cache<String,Object> cache = Caffeine.newBuilder().maximumSize(200).expireAfterAccess(second, TimeUnit.SECONDS).build();
            timedCache.put(module,cache);
            cache.put(key,data);
        }
    }

    @Override
    public <T> T getModuleCache(String key, String module) {
        if(timedCache.containsKey(module)) {
           return (T)timedCache.get(module).getIfPresent( key);
        }
        else{
            return null;
        }
    }

    @Override
    public void removeModuleCache(String key, String module) {
        if(timedCache.containsKey(module)) {
            timedCache.get(module).invalidate(key);
        }


        //timedCache.remove(module+"_"+key);
    }

    @Override
    public boolean hasModuleCache(String key, String module) {
        if(timedCache.containsKey(module)) {
           return timedCache.get(module).asMap().containsKey(key);
        }
        return false;
        //return timedCache.containsKey(module+"_"+key);
    }

    @Override
    public <T> T tryModuleCache(String key, String module, Class<T> clazz, int second, Func1<T> callback){

        if(timedCache.containsKey(module)){
            @Nullable Object v = timedCache.get(module).get(key, (k) -> {
                try {
                    return callback.invoke();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            return (T)v;
        }else {
            try {
                T v = callback.invoke();
                addModuleCache(key,v,module,second);
                return v;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
//        throw Core.makeThrow("未实现");
//        T v =  (T)timedCache.get(module+"_"+key);
//        if(v==null){
//            try {
//                v = callback.invoke();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            addModuleCache(key,v,module,second);
//            return v;
//        }
//        return v;
    }


}
