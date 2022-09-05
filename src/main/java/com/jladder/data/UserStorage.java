package com.jladder.data;

public class UserStorage {
    /**
     * 存储类型
     */
    private String name;

    private String mode="other";
    /**
     * 用户表
     */
    private String table_user;
    /**
     * 用户分组表
     */
    private String table_usergroup;


    private String field_username="username";
    private String field_uuid;

    public UserStorage(){

    }
    public UserStorage(String name, String table_user, String table_usergroup){
        this.name=name;
        this.table_user=table_user;
        this.table_usergroup=table_usergroup;
    }


    public String getName() {
        return name;
    }

    public UserStorage setName(String name) {
        this.name = name;
        return this;
    }

    public String getTable_user() {
        return table_user;
    }

    public UserStorage setTable_user(String table_user) {
        this.table_user = table_user;
        return this;
    }

    public String getTable_usergroup() {
        return table_usergroup;
    }

    public UserStorage setTable_usergroup(String table_usergroup) {
        this.table_usergroup = table_usergroup;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public UserStorage setMode(String mode) {
        this.mode = mode;
        return this;
    }
    public UserStorage setOrg() {
        this.name = "org";
        this.mode = "org";
        this.table_user="org_user";
        this.table_usergroup="org_usergroup";
        this.field_uuid="logininfo";
        return this;
    }
    public UserStorage setDeveloper() {
        this.name = "developer";
        this.mode = "developer";
        this.table_user="sys_user";
        this.table_usergroup="sys_usergroup";
        this.field_uuid="logininfo";
        return this;
    }

    public String getField_username() {
        return field_username;
    }

    public void setField_username(String field_username) {
        this.field_username = field_username;
    }

    public String getField_uuid() {
        return field_uuid;
    }

    public void setField_uuid(String field_uuid) {
        this.field_uuid = field_uuid;
    }
}
