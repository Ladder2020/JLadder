package com.jladder.utils;
import com.jladder.data.Receipt;
import com.jladder.lang.Core;
import com.jladder.lang.func.Func1;
import org.redisson.api.RBucket;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisHelper {
    /// <summary>
    /// 本实例对象
    /// </summary>
    public static RedisHelper Instance=new RedisHelper();

    private Map<Integer,RedissonClient>  Client = new HashMap<Integer, RedissonClient>();

    private String server;
    private String password;
    private int database;

    @Autowired
    public void setRedissonClient(RedissonClient client){
        Instance.Client.put(-1,client);
        //redissonClient = ;
    }
    public void setConn(String conn){
        throw Core.makeThrow("未实现");
    }
    public void setConn(String server,String password,int database){
        this.server=server;
        this.password=password;
        this.database=database;
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
        if(client==null){

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
            return new Receipt(false,e.getMessage());
        }
    }
    public Receipt<String> GetCache(String key){
        return getCache(key,-1);
    }
    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="index">数据库索引</param>
    /// <returns></returns>
    public Receipt<String> getCache(String key, int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<Object> result = client.getBucket(key);
            return new Receipt<String>().setData(result.get().toString());
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
        }
    }
    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="index">数据库索引</param>
    /// <returns></returns>
    public <T> Receipt<T> getCache(String key,Class<T> clazz){
        return getCache(key,clazz,-1);
    }
    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="index">数据库索引</param>
    /// <returns></returns>
    public <T> Receipt<T> getCache(String key,Class<T> clazz, int index){
        RedissonClient client = getClient(index);
        if(null == client)return new Receipt(false,"Redis连接对象不存在[059]");
        try{
            RBucket<T> result = client.getBucket(key);
            return new Receipt().setData(result.get());
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
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
    public Receipt delete(String key, int index)
    {
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


}
