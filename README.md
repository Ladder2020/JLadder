![logo](http://c.ladder2020.com/logo-small-java.jpg)

### **关于JLadder** 

JLadder 是 Ladder家族成员，是Ladder for java，其初版系.Net技术平台移植转化而来，目前功能转化大约60%左右，其命名规则还全部纠正，代码注释也在逐步完善中，如果关注JLadder，可在3.0版本后，用于生产环境，目前本项目处于beta阶段。~~

JLadder应用于常见类型公司，也就是不太大，有20+开发团队的公司，大多数以业务项目为主，在简单重复的劳动中，磨练出几个“精华”的东西，要说多厉害，无法比，JLadder也就是为解决一些实际应用场景的问题，专长几个技术点。无资无本，不妄谈架构，云，中台。接收“点星”和“越骂越勇”！

### JLadder能力

#### 1）数据操作能力

基本的数据库操作Dao，封装了JDBC，可选配的数据库连接池，支持多张数据库类型，数据库方言，支持原生sql语句，但不提倡。封装了Cnd条件，OrderBy，GroupBy，Curd，Sqls等常见的技术方案，很多类库都是这样做的。支持Querys[多查询],InsertSelect[查询插入],Pro[存储过程]，市面不多见，但JLadder提供，Sql执行日志可定制，比如超时时间，操作类别，数据库标记，关联请求，JLadder也是具备的。总之，JLadder差不多具备全功能DAO工具

####  2）数据模型能力

前端表单，表格行列，增删改，常常在开发过程被定义一个个实体，实体与数据库映射，美名ORM，为此进行的模型抽离，定制，继承，特别是那些set，get，各种铺天盖地的技术框架，比优秀嘛？糟透了！那个模型应该是可复用的，可定制的，可伸缩，可动态有，也可无中生有。在开发阶段是饭来张口，在运维阶段是无痛无感，升级迭代时又可参照扩展。起码要做到模型变更时，不用IDE，甚至是记事本，前后端不用那么费劲对字段。为此JLadder从一线痛苦走出。

数据源方面：常见是一个表，实实在在的一个数据库表，也可以是视图，也可以是一个接口，也可以是一个Http请求接口，也可以是JSON对象，也可以是内部方法

字段方面：数据类型，可视|可查|可写，列查询，聚合，定义表单组件，定义展示数据格式化，联想字典，字段转义，特别那些字段联动，很少有框架支持的。字段缺省值，字段生成值，UUID|AutoNum|Nuid|CreateTime|UpdateTime,虽然数据库厂商有的提供给你了，但是JLadder也提供给你。

扩展参数：假如是一个语句，外部请求参数如何传递到Sql子查询中，很多类库是用占位替代符的方式。那么当前时间如何传递，当前用户名如何传递，接口回调参数如何传递。JLadder确实能解决这些问题

模型格式: 如果是喜欢JSON格式，可以！如果喜欢XML也可以，如果你喜欢类对象也可以，如果你是平台，数据库表也是可以的

#### 3）接口路由代理能力

外部一个请求，经控制器到JLadder，JLadder可转发到内部方法上，也可以转发到Http接口上,也可以转发到MQ上，也转发到数据库操作上，也可以转发到JavaScript脚本上，我们称之为接口路由代理。这几年在微服务之流行下，服务发现，熔断降级，容器，负载均衡的方案很多，JLadder不擅长也不造轮子，JLadder做的是接口服务管理，约束定义，传输协议，平台可视化管理，无代码制造，业务接口编排，接口数据聚合等一些领域，与微服务体系技术相容相助，当然你也可以独立部署使用

传输协议：单请求|多请求[一次发起业务，多次回应结果]|回调通知请求[发起请求，异步通知结果]，请求方式定义，加密类型

调用环境:  Http服务，数据操作，内部程序，Js脚本，固定结果，Sql语句，随机结果，条件分发，路由聚合等

请求参数: 内参外参，数值校验，默认值，数据格式化，多对一参数映射，必填项协同，验证器等

数据注解: 键名解释，生成接口文档：word格式的，返回范例，

应用过程: 正式|沙盒|测试|开发，支持接口拦截器，支持限流，白名单，幂等，请求参数转换，缓存结果，数据行列的数据权限，数据节点定制

#### 4）其它平庸能力

操作日志：从请求跟控制器到数据库操作，简简单单的链路跟踪，不太强大，能用

net功能：封装了Http请求，底层用的是OKHttp，有Mail，有Socket，有FTP等，常见功能，都是抄写的

Hub：集中管理的东西，不用找东找西，都在一个hub里配置，方便一些。常见的有DataHub，WebHub， CacheHub，LatchHub

Action：封装一些快速操作方法，QueryAction是查询的，SaveAction是操作的，UpmsAction是用户登陆权限,EnvAction是环境的

Data：JLadder封装一些数据结构体，比如返回值的，分页的，结果集的，树形的

# 开始使用

#### 基本数据库操作

```
    //创建数据库信息
    DbInfo info =  new DbInfo();
    //设置数据方言
    info.setDialect(DbDialectType.SQLITE);
    info.setServer("test.db");
    //创建数据库操作对象Dao
    IDao dao = new Dao(info);
    //执行语句
    dao.exec(new SqlText("CREATE TABLE IF NOT EXISTS user (username TEXT primary key,password TEXT,email TEXT)"));
    //插入数据
    for (int i = 0; i < 100; i++) {
        dao.insert("user",new Record("username","xiaoxiao"+i).put("password","1234561"+i).put("email","xxx"+i+"@ladder.com"));
    }
    //查询
    List<Record> rs1 = dao.query("user", new Cnd("username", "like", "xiao"), new Pager(1, 20));
    //查询
    SqlText sqltext = new SqlText("select username from user where name like @name","name","xiaoxiao");
    List<Record> rs2 = dao.query(sqltext);

    String error = dao.getErrorMessage();
    //error = "[SQLITE_ERROR] SQL error or missing database (no such column: name)"
```
#### 实体操作

```
    //从配置文件加载
    Configs.LoadSettingsFromFile("config.json");
    //defaultDatabase节点是默认数据库连接，可以直接使用
    IDao dao = new Dao();
    //创建数据库表
    //dao.create(MySite.class);
    dao.exec(new SqlText("CREATE TABLE IF NOT EXISTS del_sys_site (id TEXT primary key,title TEXT,project TEXT,config_path TEXT)"));
    //创建一个实体
    MySite site = new MySite();
    site.id=Core.genUuid();
    site.setTitle("测试一下");
    site.setProject("测试项目");
    site.setOutPath("这是额外字段");//这个属性不写入数据库中
    site.setConfig_path("路径");
    //有多种方式处理，三选一
    dao.insert(site,"");//columns代表那些属性插入数据库和@Column(isExt = true)相同功效
    site.insert();//实体对象自身可以执行，但必须配置默认数据库
    site.insert(dao,"");//和dao.insert(site,"")相同
    //保存方法
    dao.save(site);//如果存在更新，不存在新增
    dao.save(site,new Cnd("project","测试项目"),"");//此时主键条件不起作用，以project='测试项目'为条件
    //查询
    List<MySite> sites = dao.query(new Cnd("project", "测试项目"), MySite.class);
    MySite site1 = dao.fetch(new Cnd("project", "测试项目"), MySite.class);
    //未封装 dao.query(site)之类的API方法，因为实际项目中实体类全部条件查询的，小肖碰到的不多
```

### 基本数据模型操作

```
    //创建一个模型
    IDataModel dm = new DataModelForMap();
    dm.TableName="sys_user";
    dm.addColumn("username","用户名","uname");
    dm.addColumn("fullname","用户名","uname");
    dm.addColumn("age","用户名","uname");
    //模型存在起来
    DataHub.WorkCache.addDataModelCache("xiaoxiao",DataModelForMapRaw.From(dm));
    //自带纠错功能，字段错了也能进库
    SaveAction.insert("xiaoxiao",new Record("xiao","dddd").put("username","dddd"));
    //SaveAction.delete(); SaveAction.update(); 等
    
    //获取数据
    QueryAction.getData("xiaoxiao",new Cnd("username","xx"),"","");
    //获取数量
    QueryAction.getCount("xiaoxiao",new Cnd("username","xx"));

```
##### 特殊用法

```
    //不建模型,格式为:*.{db}.{table}
    QueryAction.getBean("*test.sys_settings",new Cnd("name","like","基本设置"));
    SaveAction.insert("*test.sys_settings",new Record("name","基本设置"));

```

### 数据模型的集成方案

#### json模式(dm1.json)

```
{
    ticket_nodefile:{
      "name": "open_ticket_nodefile",
      "sort": "Ticket",
      "type": "table",
      "title": "工单附件表",
      "cacheitems": "",
      "depends": "",
      "tablename": "open.ticket_nodefile",
      "columns": [
        {"fieldname": "id","type": "int", "title": "ID","gen": "autonum","length": "50"},
        {"fieldname": "path","type": "string","title": "路径"},
        {"fieldname": "node_id","type": "string","title": "节点编码"},
        {"fieldname": "order_id","type": "string","title": "工单编码","lenth": 50,},
        {"fieldname": "uploader","type": "string","title": "上传者","lenth": 50},
        {"fieldname": "fullname","type": "string","title": "用户全名","lenth": 50},
        {"fieldname": "createtime","title": "创建时间","gen": "datetime","op": "=","type": "datetime","sign": "createtime"},
        {"fieldname": "showname","type": "string","title": "显示名","lenth": 500},
        {"fieldname": "order_no","type": "string","title": "工单号","lenth": 50},
        {"fieldname": "channel","type": "string","title": "系统通道","lenth": 50},
        {"fieldname": "size","type": "int","title": "文件大小","length": "11"},
        {"fieldname": "format","type": "string","title": "文件格式"},
        {"fieldname": "filepath","type": "string","title": "文件路径"}
      ],
      "queryform": "",
      "conn": "",
      "permission": "",
      "descr": "",
      "events": "",
      "script": "",
      "data": "",
      "params": ""
    }
}

```
```
//第一种：可在启动时加载,
DataHub.LoadJsonFile("dm1.json");
//或者
DataModelForMap dm = new DataModelForMap();
dm.FromJsonFile("dm1.json","ticket_nodefile");
DataHub.WorkCache.addDataModelCache("ticket_nodefile",dm.Raw);

//query或者save操作
QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));
```



#### xml格式(dm1.xml)

```
<mapping name="open_ticket_nodefile" title="工单附件表" type="table">
    <tableName>open.ticket_nodefile</tableName>
    <columns>
        <column  fieldname="id"  type="int"  title="ID"  lenth=""  isnull="false"  dvalue=""  gen="autonum"  length="50" ></column>
        <column  fieldname="path"  type="string"  title="路径"  lenth="255"  isnull="false"  dvalue="" ></column>
        <column  fieldname="node_id"  type="string"  title="节点编码"  lenth="255"  isnull="false"  dvalue="" ></column>
        <column  fieldname="order_id"  type="string"  title="工单编码"  lenth="50"  isnull="false"  dvalue="" ></column>
        <column  fieldname="uploader"  type="string"  title="上传者"  lenth="50"  isnull="false"  dvalue="" ></column>
        <column  fieldname="fullname"  type="string"  title="用户全名"  lenth="50"  isnull="false"  dvalue="" ></column>
        <column  fieldname="createtime"  title="创建时间"  gen="datetime"  op="="  type="datetime"  sign="createtime" ></column>
        <column  fieldname="showname"  type="string"  title="显示名"  lenth="500"  isnull="false"  dvalue="" ></column>
        <column  fieldname="order_no"  type="string"  title="工单号"  lenth="50"  isnull="false"  dvalue="" ></column>
        <column  fieldname="channel"  type="string"  title="系统通道"  lenth="50"  isnull="false"  dvalue="" ></column>
        <column  fieldname="size"  type="int"  title="文件大小"  lenth=""  isnull="false"  dvalue=""  length="11" ></column>
        <column  fieldname="format"  type="string"  title="文件格式"  lenth="10"  isnull="false"  dvalue="" ></column>
        <column  fieldname="filepath"  type="string"  title="文件路径"  lenth="500"  isnull="false"  dvalue="" ></column>
    </columns>
    <data></data>
    <conn></conn>
</mapping>

```
```
//加载xml模型文件
DataHub.LoadXmlFile("dm1.xml");
//query或者save操作
QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));
```

注意:json和xml的文件管理和存放位置，可根据项目和个人习惯自由处理，另外目前JLadder没有文件监视功能，文件变更时，手动调用DataHub.WorkCache.removeAllDataModelCache();

#### 数据库模式

```
CREATE TABLE `lader_models` (
  `Id` varchar(100) NOT NULL,
  `sort` varchar(100) DEFAULT NULL COMMENT '分属',
  `Name` varchar(100) DEFAULT NULL COMMENT '键名',
  `TableName` text COMMENT '真实表名',
  `Title` varchar(100) DEFAULT NULL COMMENT '标题',
  `Columns` text COMMENT '列模型',
  `QueryForm` text COMMENT '表单查询',
  `Type` varchar(100) DEFAULT NULL COMMENT '类型',
  `Enable` int(11) DEFAULT '1' COMMENT '使能位',
  `Fromer` varchar(100) DEFAULT NULL COMMENT '来源处',
  `Permission` varchar(100) DEFAULT NULL COMMENT '权限',
  `Data` longtext COMMENT '交换数据',
  `Updatetime` datetime DEFAULT NULL COMMENT '更新时间',
  `Events` text COMMENT '事件列表',
  `Script` text COMMENT '脚本代码',
  `Params` text COMMENT '参数列表',
  `Conn` varchar(500) DEFAULT NULL COMMENT '连接器',
  `Descr` varchar(500) DEFAULT NULL COMMENT '备注说明',
  `VisitTimes` varchar(200) DEFAULT '0' COMMENT '访问次数',
  `CacheItems` varchar(50) DEFAULT NULL,
  `Depends` varchar(500) DEFAULT NULL COMMENT '依赖项',
  `AnalyzeItems` varchar(200) DEFAULT NULL COMMENT '分析项目',
  `IsDelete` int(11) DEFAULT '0' COMMENT '是否已删除',
  PRIMARY KEY (`Id`) USING BTREE,
  KEY `sys_data_name` (`Name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8

```

```
//配置模型数据库
DataHub.TemplateTableName="lader_models";
DataHub.TemplateConn="defaultDatabase";//defaultDatabase时，可以忽略

//query或者save操作
QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));
```



   

