package com.jladder.es;

/**
 * @author YiFeng
 * @date 2022年04月19日 15:20
 */
public final class ElasticEntity<T> {
    private String id;
    private T data;

    public ElasticEntity() {
    }

    public ElasticEntity(String id, T data) {
        this.data = data;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
