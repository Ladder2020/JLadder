package com.jladder.data;

/***
 * 分页对象，
 * 注意不要修改本类的大小写，而且data包下的所有类，都是一些数据格式类，大小写都不应修改
 */
public class Pager {

    /**
     * 页号
     */
    public int pageNumber;
    /**
     * 页大小
     */
    public int pageSize;

    /**
     * 页数据
     */
    public int pageCount;

    /**
     * 记录数量
     */
    public long recordCount;
    /**
     * 页的起始位置
     */
    public int pageoffset=-1;
    /**
     *
     */
    public String field="";

    /**
     *
     */
    public Pager(){
        pageNumber = 1;
        pageSize = 20;
    }

    /***
     *
     * @param pageNo
     * @param pageSize
     */
    public Pager(int pageNo,int pageSize){
        this.pageNumber = pageNo > 0 ? pageNo : 1;
        this.pageSize = pageSize > 0 ? pageSize : 20;
    }

    /**
     *
     * @param pageSize
     */
    public Pager(int pageSize){
        this.pageNumber = 1;
        this.pageSize = pageSize > 0 ? pageSize : 20;
    }

    /**
     * 设置起始位置
     * @param offset
     * @return
     */
    public Pager setOffset(int offset){
        this.pageoffset = offset;
        return this;
    }

    /**
     * 设置分页字段
     * @param field 字段名称
     * @return
     */
    public Pager setField(String field){
        this.field = field;
        return this;
    }

    /***
     * 获取偏离
     * @return
     */
    public int getOffset(){
        if (this.pageoffset < 0)this.pageoffset= pageSize * (pageNumber - 1);
        return this.pageoffset;
    }

    /**
     * 获取页数量
     * @return
     */
    public int GetPageCount(){
        if (pageCount < 0) pageCount = (int)Math.ceil((double)recordCount / pageSize);
        return pageCount;
    }

    /**
     * 设置总记录数量
     * @param recordCount 总记录数量
     * @return
     */
    public Pager setRecordCount(long recordCount){
        this.recordCount = recordCount > 0 ? recordCount : 0;
        this.pageCount = (int)Math.ceil((double)recordCount / pageSize);
        return this;
    }

    /**
     * 获取当前的页号
     * @return
     */

    public int getPageNumber()
    {
        return pageNumber;
    }

    /**
     *获取页大小
     * @return
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * 获取总记录数量
      * @return
     */
    public long getRecordCount()
    {
        return recordCount;
    }
    /***
     * 设置页号
     * @param pageNumber 页号
     * @return
     */
    public Pager setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
        this.pageoffset = pageSize * (pageNumber - 1);
        return this;
    }
    /***
     * 设置页大小
     * @param pageSize 页大小
     * @return
     */
    public Pager setPageSize(int pageSize){
        this.pageSize = (pageSize > 0 ? pageSize : 20);
        return resetPageCount();
    }
    /**
     * 重置页计数量
     * @return
     */
    public Pager resetPageCount(){
        pageCount = -1;
        return this;
    }
    /**
     * 是否是第一页
     * @return
     */
    public boolean isFirst()
    {
        return pageNumber == 1;
    }
    /**
     * 是否是最后一页
     * @return
     */
    public boolean isLast(){
        if (pageCount == 0) return true;
        return pageNumber == pageCount;
    }

    /**
     * 重置分页起始
     */
    public void reset(){
        this.reset(true);
    }
    /**
     * 重置分页起始
     * @param isByPageNo
     */
    public void reset(boolean isByPageNo){
        this.recordCount = recordCount > 0 ? recordCount : 0;
        this.pageCount = (int)Math.ceil((double)recordCount / pageSize);
        if (isByPageNo){
            this.pageoffset = -1;
        }
        else{
            this.pageNumber = 0;
        }
    }

}
