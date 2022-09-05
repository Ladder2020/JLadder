package com.jladder.es;

import com.jladder.data.Pager;

import java.util.Date;
import java.util.List;

/**
 * @author YiFeng
 * @date 2022年04月20日 17:11
 */
public class ElasticSearchResult {
    private List<ElasticSearchRecord> records;
    private Pager pager;
    private int statusCode= 200;
    private String message="查询成功";
    private long duration=0;

    /**
     * 初始化
     * @param records 记录集
     */
    public ElasticSearchResult(List<ElasticSearchRecord> records) {
        this.records = records;
    }

    /**
     * 初始化
     * @param statusCode 状态码
     * @param message 返回消息
     */
    public ElasticSearchResult(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * 获取记录集
     * @return
     */
    public List<ElasticSearchRecord> getRecords() {
        return records;
    }

    /**
     * 设置记录集
     * @param records 记录集
     * @return
     */
    public ElasticSearchResult setRecords(List<ElasticSearchRecord> records) {
        this.records = records;
        return this;
    }

    /**
     * 获取分页对象
     * @return
     */
    public Pager getPager() {
        return pager;
    }

    /**
     * 设置分页对象
     * @param pager 分页对象
     * @return
     */
    public ElasticSearchResult setPager(Pager pager) {
        this.pager = pager;
        return this;
    }

    /**
     * 获取状态码
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 设置状态码
     * @param statusCode 状态码
     * @return
     */
    public ElasticSearchResult setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * 获取返回消息
     * @return
     */
    public String getMessage() {
        return message;

    }

    /**
     * 设置返回消息
     * @param message 返回消息
     * @return
     */
    public ElasticSearchResult setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置分页对象
     * @param pageSize 页大小
     * @param recordCount 记录总数
     * @return
     */
    public ElasticSearchResult setPager(int pageSize,int recordCount){
        this.pager=new Pager(1,pageSize);
        this.pager.setRecordCount(recordCount);
        return this;
    }

    /**
     * 设置页号
     * @param number 页号
     * @return
     */
    public ElasticSearchResult setPageNumber(int number){
        if(this.pager==null)this.pager=new Pager();
        this.pager.setPageNumber(number);
        return this;
    }

    /**
     * 获取时长
     * @return
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 设置时长
     * @param duration 时长ms
     * @return
     */
    public ElasticSearchResult setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * 设置时长
     * @param start 开始时间
     * @return
     */
    public ElasticSearchResult setDuration(Date start){
        long time = new Date().getTime() - start.getTime();
        this.duration = time;
        return this;
    }
}

