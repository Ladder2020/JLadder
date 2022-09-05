package com.jladder.es;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Json;
import com.jladder.lang.Strings;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.util.*;

public class ElasticSearch {

    /**
     * 静态实例
     */
    private volatile static ElasticSearch instance;

    /**
     * 获取静态实例
     */
    public static ElasticSearch getInstance() {
        if (instance == null) {
            synchronized (ElasticSearch.class) {
                if (instance == null) {
                    instance = new ElasticSearch();
                }
            }
        }
        return instance;
    }
    private static final NamedXContentRegistry xContentRegistry;

    static {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());

        xContentRegistry = new NamedXContentRegistry(searchModule.getNamedXContents());
    }

    RestHighLevelClient client;
    private final Map<String,Object> config= new HashMap<>();


    /**
     * 初始化
     * @param hosts 主机列表
     * @param username 用户名
     * @param password 密码
     * @return void 
     * @author YiFeng
     * @date 2022/4/19 15:24
     */
    
    public RestHighLevelClient init(List<HttpHost> hosts,String username,String password){
        config.put("hosts",hosts);
        config.put("username",username);
        config.put("password",password);
        return init();
    }
    /**
     * 初始化
     * @return void 
     * @author YiFeng
     * @date 2022/4/19 15:24
     */
    
    public RestHighLevelClient init(){
        try {
            if(client!=null){
                client.close();
                client=null;
            }
            List<HttpHost> hosts = (List<HttpHost>)config.get("hosts");
            String username = (String)config.get("username");
            if(hosts==null || hosts.size()<1)return client;
            RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]));

            if(Strings.hasValue(username)){
                String password = (String)config.get("password");
                builder.setHttpClientConfigCallback(b->{
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(username, password));
                    return b.setDefaultCredentialsProvider(credentialsProvider);
                });
            }
            client=new RestHighLevelClient(builder);
        }catch (Exception e){
            e.printStackTrace();
        }
        return client;
    }

    ///#region 主机信息

    /**
     * 添加主机
     * @param host 主机信息
     * @return com.jladder.es.ElasticSearch 
     * @author YiFeng
     * @date 2022/4/19 16:20
     */
    
    public ElasticSearch addHost(HttpHost host){
        if(host==null)return this;
        List<HttpHost> hosts = (List<HttpHost>) config.get("hosts");
        if(hosts==null){
            hosts = new ArrayList<HttpHost>();
            config.put("hosts",hosts);
        }
        hosts.add(host);
        return this;
    }
    /**
     * 设置用户名
     * @param username 用户名
     * @return com.jladder.es.ElasticSearch 
     * @author YiFeng
     * @date 2022/4/19 16:21
     */
    
    public ElasticSearch setUserName(String username){
        config.put("username",username);
        return this;
    }
    /**
     * 设置密码
     * @param password 密码
     * @return com.jladder.es.ElasticSearch 
     * @author YiFeng
     * @date 2022/4/19 16:21
     */
    
    public ElasticSearch setPassword(String password){
        config.put("password",password);
        return this;
    }
    ///endregion

    /**
     * 索引是否存在
     * @param index 索引名称
     * @return boolean
     * @author YiFeng
     * @date 2022/4/19 11:24
     */
    public boolean indexExist(String index){
        try{
            GetIndexRequest request = new GetIndexRequest(index);
            request.local(false);
            request.humanReadable(true);
            request.includeDefaults(false);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建索引
     * @param index 索引名称
     * @param shards 分片数
     * @param replicas 副本数
     * @param properties 属性配置
     * @return
     */
    public boolean indexCreate(String index,int shards,int replicas,String properties){
        try {
            CreateIndexRequest request=new CreateIndexRequest(index);
            request.settings(Settings.builder().put("index.number_of_shards", shards).put("index.number_of_replicas", replicas));
            if(Strings.hasValue(properties))request.mapping(properties, XContentType.JSON);
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 
     * @param index 
     * @return void 
     * @author YiFeng
     * @date 2022/4/19 15:16
     */
    
    public void deleteIndex(String index) {
        try {
            client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 保存数据
     * @param index 索引名称
     * @param data 数据对象
     * @return com.jladder.data.Receipt<java.lang.String>
     * @author YiFeng
     * @date 2022/4/19 15:41
     */

    public Receipt<String> save(String index, Object data){
        IndexRequest request = new IndexRequest(index);
        request.source(Json.toJson(data),XContentType.JSON);
        try {
            IndexResponse res = client.index(request, RequestOptions.DEFAULT);
            return res==null?new Receipt<String>(false):new Receipt<String>(Strings.isBlank(res.getId())).setData(res.getId());

        } catch (Exception e) {
            return Receipt.create(e);
        }
    }
    /**
     * 保存数据
     * @param index 索引名称
     * @param data 数据对象
     * @param id 数据ID
     * @return com.jladder.data.Receipt<java.lang.String>
     * @author YiFeng
     * @date 2022/4/19 15:41
     */
    public Receipt<String> save(String index, Object data,String id){
        IndexRequest request = new IndexRequest(index);
        if(Strings.hasValue(id))request.id(id);
        request.source(Json.toJson(data),XContentType.JSON);
        try {
            IndexResponse res = client.index(request, RequestOptions.DEFAULT);
            return res==null?new Receipt<String>(false):new Receipt<String>(Strings.isBlank(res.getId())).setData(res.getId());

        } catch (Exception e) {
            return Receipt.create(e);
        }
    }
    /**
     * 新增或更新一段数据
     * @param index 索引名称
     * @param entity
     * @return void
     * @author YiFeng
     * @date 2022/4/19 15:21
     */
    public Receipt<String> save(String index, ElasticEntity entity) {
        IndexRequest request = new IndexRequest(index);
        request.id(entity.getId());
        request.source(Json.toJson(entity.getData()), XContentType.JSON);
        try {
            IndexResponse res = client.index(request, RequestOptions.DEFAULT);
            return res==null?new Receipt<String>(false):new Receipt<String>(Strings.isBlank(res.getId())).setData(res.getId());
        } catch (Exception e) {
            return Receipt.create(e);
        }
    }
    /**
     * 批量保存
     * @param index  索引名称
     * @param list 数据列表
     * @return void 
     * @author YiFeng
     * @date 2022/4/19 16:03
     */
    
    public Receipt saves(String index, Collection<ElasticEntity> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(index).id(item.getId()).source(Json.toJson(item.getData()), XContentType.JSON)));
        try {
            BulkResponse res = client.bulk(request, RequestOptions.DEFAULT);
            return new Receipt(res.status()== RestStatus.OK).setData(res.getItems());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 批量保存
     * @param index  索引名称
     * @param rs 记录集
     * @return void
     * @author YiFeng
     * @date 2022/4/19 16:03
     */

    public Receipt saves(String index, List<Record> rs,String fieldname) {
        BulkRequest request = new BulkRequest();
        rs.forEach(item -> request.add(new IndexRequest(index).id(item.getString(fieldname)).source(Json.toJson(item), XContentType.JSON)));
        try {
            BulkResponse res = client.bulk(request, RequestOptions.DEFAULT);
            return new Receipt(res.status()== RestStatus.OK).setData(res.getItems());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 批量删除
     * @param index 索引名称
     * @param ids id集合
     * @author YiFeng
     * @date 2022/4/19 15:55
     */
    public <T> void deletes(String index, Collection<T> ids) {
        BulkRequest request = new BulkRequest();
        ids.forEach(item -> request.add(new DeleteRequest(index, item.toString())));
        try {
            BulkResponse res = client.bulk(request, RequestOptions.DEFAULT);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为SearchSourceBuilder对象
     * @param search es查询语句
     * @return org.elasticsearch.search.builder.SearchSourceBuilder
     * @author YiFeng
     */
    public SearchSourceBuilder toSearchSourceBuilder(String search){
        SearchSourceBuilder builder = new SearchSourceBuilder();
        try{
            XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(xContentRegistry,LoggingDeprecationHandler.INSTANCE,search);
            builder.parseXContent(parser);
        }catch (Exception e){
            e.printStackTrace();
        }
        return builder;
    }

    /**
     * 搜素数据
     * @param index 索引名称
     * @param builder 搜索条件
     * @param clazz 实体类型
     * @return java.util.List<T> 
     * @author YiFeng
     * @date 2022/4/19 16:19
     */
    
    public <T> List<T> search(String index, SearchSourceBuilder builder, Class<T> clazz) {
        SearchRequest request = new SearchRequest(index);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(Json.toObject(hit.getSourceAsString(), clazz));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 普通搜索
     * @param index 索引名称
     * @param builder 搜索条件
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @author YiFeng
     * @date 2022/4/19 16:19
     */
    public ElasticSearchResult search(String index, SearchSourceBuilder builder) {
        Date start = new Date();
        SearchRequest request = new SearchRequest(index);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<ElasticSearchRecord> res = new ArrayList<ElasticSearchRecord>(hits.length);
            for (SearchHit hit : hits) {
                res.add(ElasticSearchRecord.from(hit));
            }
            return new ElasticSearchResult(res).setPager(builder.size(),(int)response.getHits().getTotalHits().value).setDuration(start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 高亮搜索
     * @param index 索引名称
     * @param builder 搜索条件
     * @return java.util.List<com.jladder.es.ElasticSearchResult>
     * @author YiFeng
     */
    public ElasticSearchResult highlight(String index, SearchSourceBuilder builder){
        Date start = new Date();
        if(builder==null)return null;
        if(builder.highlighter()==null){
            HighlightBuilder high = new HighlightBuilder();
            high.preTags("<span style='color:red;'>");//手动前缀标签
            high.postTags("</span>");
            high.field("*");
            builder.highlighter(high);
        }
        SearchRequest request = new SearchRequest(index);
        request.source(builder);

        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<ElasticSearchRecord> res = new ArrayList<>(hits.length);
            for (SearchHit hit : hits) {
                res.add(ElasticSearchRecord.from(hit));
            }
            return new ElasticSearchResult(res).setPager(builder.size(),(int)response.getHits().getTotalHits().value).setDuration(start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 高亮搜索
     * @param index 索引名称
     * @param builder 搜索条件
     * @param high 高亮设置
     * @return java.util.List<com.jladder.es.ElasticSearchResult>
     * @author YiFeng
     */
    public ElasticSearchResult search(String index, SearchSourceBuilder builder,HighlightBuilder high) {
        Date start = new Date();
        if(high!=null)builder.highlighter(high);
        SearchRequest request = new SearchRequest(index);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<ElasticSearchRecord> res = new ArrayList<ElasticSearchRecord>(hits.length);
            for (SearchHit hit : hits) {
                res.add(ElasticSearchRecord.from(hit));
            }
            return new ElasticSearchResult(res).setPager(builder.size(),(int)response.getHits().getTotalHits().value).setDuration(start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
