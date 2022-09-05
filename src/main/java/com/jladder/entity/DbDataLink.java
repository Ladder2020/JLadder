package com.jladder.entity;

import com.jladder.db.annotation.Table;


/// <summary>
/// 代理服务表
/// </summary>
@Table("sys_datalink")
public class DbDataLink {
        /// <summary>
        /// ID
        /// </summary>
        public String id;
        /// <summary>
        /// 键名
        /// </summary>
        public String name;

        /// <summary>
        /// 标题
        /// </summary>
        public String title;
        /// <summary>
        /// 数据源
        /// </summary>
        public String datasource;
        /// <summary>
        /// 映射字段
        /// </summary>
        public String mappings;
        /// <summary>
        /// 事件
        /// </summary>
        public String events;
        /// <summary>
        /// 是否有效
        /// </summary>
        public int enable;
        /// <summary>
        /// 上级归属
        /// </summary>
        public String pid;
        /// <summary>
        /// 备注说明
        /// </summary>
        public String descr;

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getTitle() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public String getDatasource() {
                return datasource;
        }

        public void setDatasource(String datasource) {
                this.datasource = datasource;
        }

        public String getMappings() {
                return mappings;
        }

        public void setMappings(String mappings) {
                this.mappings = mappings;
        }

        public String getEvents() {
                return events;
        }

        public void setEvents(String events) {
                this.events = events;
        }

        public int getEnable() {
                return enable;
        }

        public void setEnable(int enable) {
                this.enable = enable;
        }

        public String getPid() {
                return pid;
        }

        public void setPid(String pid) {
                this.pid = pid;
        }

        public String getDescr() {
                return descr;
        }

        public void setDescr(String descr) {
                this.descr = descr;
        }





}
