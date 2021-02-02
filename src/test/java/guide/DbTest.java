package guide;

import bean.MySite;
import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.DbInfo;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.enums.DbGenType;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.lang.Core;
import com.jladder.lang.Times;
import org.junit.Assert;
import org.junit.Test;
import java.sql.SQLException;
import java.util.List;

public class DbTest {

	@Test
	public void connTest() throws SQLException {
		//创建数据库信息
		DbInfo info =  new DbInfo();
		info.setDialect(DbDialectType.SQLITE);
		info.setServer("test.db");
		//创建数据库操作对象Dao
		IDao dao = new Dao(info);
		//执行语句
		dao.exec(new SqlText("CREATE TABLE IF NOT EXISTS user (username TEXT primary key,password TEXT,email TEXT)"));
		//插入数据
		dao.insert("user",new Record("username","xiaoxiao").put("password","123456"));

	}

	@Test
	public void TestBaseSupport1() {

		int i = 0;
		// Setting setting2 = new Setting("config/db.setting");
		// Setting setting3 = new Setting("config/1db1.setting");
		// Setting setting1 = new Setting("config/ttt.setting");
		//
		//
		// DSFactory.create(setting1);
		 Dao dao = new Dao("database12");

//		 "select * from user where name=?"
//		"select * from user where name=@name and age=@age"

		final List<Record> rs = dao.query(new SqlText("select * from user where name=@name or age=@age or att=@att", "age,name,att", "18"));
		//Assert.assertEquals("王五", name);

//		List<Entity> find = null;
//		try {
//			find = Db.use().query("select * from user where age = ?", 18);
//			Assert.assertEquals("王五", find.get(0).get("name"));
//		} catch (SQLException throwables) {
//			throwables.printStackTrace();
//		}

	}
	@Test
	public void testDao_query(){
		Dao dao = new Dao();
		Record record = new Record();
		record.put("title","xiaoxiao1");
		record.put("project","project1");

		//dao.update("dd",record,new Cnd("filename","=",12, Or).put("").and(new CNd()))


		String title = dao.getValue(SqlText.create("select title from del_sys_site limit 1"),String.class);
		//dao.save("tablename",record,new Cnd())
		int row = dao.save("del_sys_site",record,"id",DbGenType.ID);
		dao.getErrorMessage();
		Assert.assertEquals(row,1);
		int tt = dao.exec(new SqlText(),(r,conn)->{
			return r;
		});

	}
	@Test
	public void testDao_pager(){
		Dao dao = new Dao("mysql");

		Record record = dao.fetch(SqlText.create("select * from sys_user where username = @name", "name", "xiaoxiao"));
		List<Record> r1 = dao.query("sys_user", new Cnd("username", "xiaoxiao"), new Pager(1, 1));
//		SqlText sql = dao.PagingSqlText(SqlText.create("select * from user"), new Pager(0, 20), DbDialectType.MYSQL);
		SqlText sql = dao.pagingSqlText(SqlText.create("select * from user"), new Pager(5, 20).setField("id"), DbDialectType.SQLSERVER);

		System.out.println(sql.cmd);

	}

	@Test
	public  void  testquery()
	{

		Dao dao=new Dao("mysql1");
		//List<Record>  r= dao.query(new SqlText("select * from jtabletest"));


		dao.beginTran();
		for(Integer i=0;i<5;i++)
		{

			Record bean = new Record("id","").put("name", "动物"+i.toString()).put("value","王八666").put("is_delete","1");
		//	if(i==2) bean.put("id","3");

			dao.save("jtabletest",bean,"id",DbGenType.ID);
			if(!dao.getErrorMessage().isEmpty()){
				dao.rollback();
				break;
			}



		}
		//dao.rollback();
		boolean as=dao.commitTran();







		//dao.delete("jtabletest",new Cnd("id","<>","123"));

	}


	@Test
	public void testInsert(){

		Dao dao = new Dao("mysql");
		MySite site = new MySite();

		site.id = Core.genUuid();
		site.title ="ceshi";
		site.config_path = Times.getNow();
		site.outPath = "xiaoxiao";







		Record bean = new Record("title","dd").put("id", Core.genUuid()).put("config_path","224").put("xiaozhuo",111);
		int ddt = dao.insert("del_sys_site",site,"id,config_path,title,project",true);

	}
	@Test
	public void TestBeanTool(){
		MySite site = new MySite();
		site.id = Core.genUuid();
		site.title ="ceshi";
		site.config_path = Times.getNow();
		site.outPath = "xiaoxiao";
		Record bean = site.GenBean(DbSqlDataType.Insert);
		System.out.println(bean);
	}
	@Test
	public void TestEntry(){
		MySite site = new MySite();
		site.id = "176657af3a5c41b9a29ef90c75589e48";
		site.title ="标题1";
		site.config_path = Times.getNow();
		site.outPath = "xiaoxiao";
		IDao dao = new Dao("mysql");
//		int row = site.insert(dao,"id,title,config_path");
		site.title ="校长呀222";
		int row = site.update(dao);


		List<MySite> rs = site.select(dao, null);
	}
}