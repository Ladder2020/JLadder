package com.jladder.actions;

import com.jladder.data.Record;

import java.util.List;

/// <summary>
    /// 锁存器实现接口
    /// </summary>
    public abstract class ILatch
    {
        /// <summary>
        /// 设置缓存方法
        /// </summary>
        /// <param name="key">键名</param>
        /// <param name="data">数据</param>
        public abstract void SetCache(String key, Object data);
        /// <summary>
        /// 获取缓存方法
        /// </summary>
        /// <typeparam name="T">泛型类型</typeparam>
        /// <param name="key">键名</param>
        /// <returns></returns>
        public abstract <T> T GetCache(String key);
        /// <summary>
        /// 获取全部数据缓存
        /// </summary>
        /// <param name="key">键名</param>
        /// <returns></returns>
        public abstract List<Record> GetTableCache(String key);
        /// <summary>
        /// 设置全部数据缓存
        /// </summary>
        /// <param name="key">键名</param>
        public abstract void SetTableCache(String key);
        /// <summary>
        /// 移除缓存的通知
        /// </summary>
        /// <param name="key">键名</param>
        public abstract void RemoveNotice(String key);


    }
