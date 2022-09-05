package com.jladder.utils;
import com.jladder.data.Receipt;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Func1;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisHelper {
    /// <summary>
    /// 本实例对象
    /// </summary>
    public static RedisHelper Instance=new RedisHelper();

    private Map<Integer,RedissonClient>  Client = new HashMap<Integer, RedissonClient>();

    private String host;
    private String password="";
    private int database=1;
    private int port=6379;

    @Autowired
    public void setRedissonClient(RedissonClient client){
        Instance.Client.put(-1,client);
        //redissonClient = ;
    }
    public boolean isConfigured(){
        if(Client!=null && Client.size()>0)return true;
        if(Strings.hasValue(host))return true;
        return false;
    }
    public void setConn(String conn){
        throw Core.makeThrow("未实现");
    }
    public void setConn(String host,int port,String password,int database){
        this.host=host;
        this.password=password;
        this.database=database;
        this.port=port;
    }
    public void setConn(String host,int port,String password){
        this.host=host;
        this.password=password;
        this.port=port;
    }
    public void setConn(String host,String password,int database){
        this.host=host;
        this.password=password;
        this.database=database;
        this.port=6379;
    }
    public void setConn(String host,String password){
        this.host=host;
        this.password=password;
        this.database=1;
        this.port=6379;
    }
    /***
     *
     * @return
     */
    public RedissonClient getClient(){
        return getClient(-1);
    }

    /***
     * 获取客户端
     * @param index
     * @return
     */
    public RedissonClient getClient(int index){
        RedissonClient client = Client.get(index);
        if(client==null && Strings.hasValue(host)){
            synchronized (this){
                Config config = new Config();
                //config.useClusterServers().addNodeAddress("127.0.0.1:6379");
                SingleServerConfig server = config.useSingleServer().setAddress("redis://" + host + ":" +port).setDatabase(index<0?database:index);
                if(Strings.hasValue(password))server.setPassword(password);
                client = Redisson.create(config);
                Client.put(index,client);
            }
        }
        return client;
    }


    /***
     * 添加持久缓存
     * @param key 键名
     * @param data 数据对象
     * @return
     */
    public Receipt addCache(String key,Object data){
        RedissonClient client = getClient(-1);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<Object> result = client.getBucket(key);
            result.set(data);
            return new Receipt();
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
        }

    }

    /***
     *
     * @param key
     * @param data
     * @param minute
     * @return
     */
    public Receipt addCache(String key, Object data, int minute){
        return addCache(key,data,minute,-1);
    }

    /***
     * 添加持久缓存
     * @param key 键名
     * @param data 数据对象
     * @param minute 过期分钟数
     * @param index 数据库索引
     * @return
     */
    public Receipt addCache(String key, Object data, int minute,int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<Object> result = client.getBucket(key);
            result.set(data,minute,TimeUnit.MINUTES);
            return new Receipt();
        }catch (Exception e){
            e.printStackTrace();
            return new Receipt(false,e.getMessage());
        }
    }
    public Receipt<String> getString(String key){
        return getCache(key,-1);
    }




    /***
     * 获取全部的键
     * @param index
     * @return com.jladder.data.Receipt<java.util.List<java.lang.String>>
     * @author YiFeng
     */
    public Receipt<List<String>> getKeys(int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[149]");
        try{
            RKeys result = client.getKeys();
            if(result==null)return new Receipt(false,"键集合不存在[152]");
            List<String> list = result.getKeysStream().collect(Collectors.toList());
            return new Receipt().setData(list);
        }catch (Exception e){
            e.printStackTrace();
            return new Receipt(false,e.getMessage());
        }
    }

    /**
     * 获取缓存
     * @param key 键名
     * @param index 数据库索引
     * @return
     */
    public Receipt<String> getString(String key, int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<Object> result = client.getBucket(key);
            return new Receipt<String>().setData(result.get().toString());
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
        }
    }

    /**
     * 获取缓存
     * @param key 键名
     * @param clazz 类型泛型
     * @param <T>
     * @return
     */
    public <T> Receipt<T> getCache(String key,Class<T> clazz){
        return getCache(key,-1);
    }
    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="index">数据库索引</param>
    /// <returns></returns>
    public <T> Receipt<T> getCache(String key,int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<T> result = client.getBucket(key);
            return new Receipt<T>().setData(result.get());
        }catch (Exception e){
            return new Receipt<T>(false,e.getMessage());
        }
    }
    public <T> Receipt<T> popCache(String key,Class<T> clazz){
        return popCache(key,clazz,-1);
    }
    public <T> Receipt<T> popCache(String key,Class<T> clazz,int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[160]");
        RBucket<T> result = client.getBucket(key);
        if(!result.isExists())return new Receipt(false);
        try{
            T v = result.get();
            result.delete();
            return new Receipt().setData(v);
        }catch (Exception e){
            return new Receipt(false);
        }
    }
    public <T>  T get(String key,  Func1<T> callback) throws Exception {
        RedissonClient client = getClient(-1);
        if(null == client)return null;
        RBucket<T> result = client.getBucket(key);
        if(result.isExists()){
            return result.get();
        }else{
            T t = callback.invoke();
            if(t!=null)addCache(key,t);
            return t;
        }
    }


    public Receipt delete(String key){
        return delete(key,-1);
    }
    public Receipt delete(String key, int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try
        {
            RBucket<Object> result = client.getBucket(key);
            if(result==null)return new Receipt(true,"此键不存在");
            return new Receipt(result.delete());
        }
        catch(Exception e)
        {
            return new Receipt(false,e.getMessage());
        }
    }
    public Receipt clear(int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try {
            client.getKeys().flushall();
            return new Receipt();
        }
        catch(Exception e)
        {
            return new Receipt(false,e.getMessage());
        }
    }

    public String getString1(String key) {
        RBucket<Object> result = getClient(-1).getBucket(key);
        return result.get().toString();
    }

    public void setString1(String key, Object value) {

        RBucket<Object> result = getClient().getBucket(key);
        if (!result.isExists()) result.set(value, 20, TimeUnit.MINUTES);
    }
    public void setString1(String key, Object value,int min) {

        RBucket<Object> result = getClient().getBucket(key);
        if (!result.isExists()) result.set(value, min, TimeUnit.MINUTES);
    }

    public boolean hasKey(String key) {
        RedissonClient client = getClient();
        if(client==null)return false;
        RBucket<Object> result = client.getBucket(key);
        if (result.isExists()) {
            return true;
        } else {
            return false;
        }
    }
    public long incr(String key, long delta) {
        return getClient().getAtomicLong(key).addAndGet(delta);
    }
    // -----------------------------------------------------------------------

    public void lock() {
        RCountDownLatch countDown = getClient().getCountDownLatch("aa");
        countDown.trySetCount(1);
        try {
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RCountDownLatch latch = getClient().getCountDownLatch("countDownLatchName");
        latch.countDown();
        RReadWriteLock rwlock = getClient().getReadWriteLock("lockName");
        rwlock.readLock().lock();
        rwlock.writeLock().lock();
        rwlock.readLock().lock(10, TimeUnit.SECONDS);
        rwlock.writeLock().lock(10, TimeUnit.SECONDS);
        try {
            boolean res = rwlock.readLock().tryLock(100, 10, TimeUnit.SECONDS);
            boolean res1 = rwlock.writeLock().tryLock(100, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ///region 操作 Map

    public <T> Receipt<RMapCache<String, T>> getMapCache(String map,int index){
        try {
            RedissonClient client = getClient(index);
            if(null == client)return new Receipt(false,"Redis连接对象不存在[332]");
            return new Receipt<RMapCache<String, T>>().setData(client.getMapCache(map));
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
        }
    }

    public <T> void setMapObject(String token, T object,String mapName,Long minutes,int index) {
        try{
            RMapCache<String, T> map = getClient(index).getMapCache(mapName);
            map.put(token, object, minutes, TimeUnit.MINUTES);
            map.clearExpireAsync();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public Object getMapObject(String key, String mapName,int index) {
        RMapCache<String, Object> map = getClient(index).getMapCache(mapName);
        return map.get(key);
    }
    public boolean updateExpire(String mapName, String key, long ttl, TimeUnit ttlUnit,int index) {
        RMapCache<String, Object> map = getClient(index).getMapCache(mapName);
        return map.updateEntryExpiration(key, ttl, ttlUnit, 0, ttlUnit);
    }

    //操作 Map
    public void delMapObject(String mapName, String key,int index) {
        RMapCache<String, Object> map = getClient(index).getMapCache(mapName);
        map.remove(key);
    }

    public void delMapObjects(String mapName, List<String> keys,int index) {
        RMapCache<String, Object> map = getClient(index).getMapCache(mapName);
        keys.stream().forEach(key -> {
            map.remove(key);
        });
    }
    public void setMapObject(String token, Object object, String mapName, long ttl,TimeUnit unit,int index) {
        RMapCache<String, Object> map = getClient(index).getMapCache(mapName);
        map.put(token, object,ttl, unit);

    }

    ///endregion

}
