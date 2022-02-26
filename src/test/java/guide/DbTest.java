package guide;

import bean.MySite;
import com.jladder.configs.Configure;
import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.DbInfo;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.lang.Core;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;



public class DbTest {

	@Test
	public void connTest() throws SQLException {
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
		//获取sql错误
		String error = dao.getErrorMessage();
		//error = "[SQLITE_ERROR] SQL error or missing database (no such column: name)"

	}

	@Test
	public void BeanTest() {
		//从配置文件加载
		Configure.loadSettingsFromFile("config.json");
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



	}

}