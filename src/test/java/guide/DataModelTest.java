package guide;

import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.data.Record;
import com.jladder.datamodel.DataModelForMap;
import com.jladder.datamodel.DataModelForMapRaw;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.Cnd;
import com.jladder.hub.DataHub;

public class DataModelTest {


    public void initTest(){
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
    }
    public void lessTest(){
        //不建模型,格式为:*.{db}.{table}
        QueryAction.getBean("*test.sys_settings",new Cnd("name","like","基本设置"));
        SaveAction.insert("*test.sys_settings",new Record("name","基本设置"));
    }

    public void JsonTest(){
        //第一种：可在启动时加载,
        DataHub.LoadJsonFile("dm1.json");
        //或者
        DataModelForMap dm = new DataModelForMap();
        dm.FromJsonFile("dm1.json","ticket_nodefile");
        DataHub.WorkCache.addDataModelCache("ticket_nodefile",dm.Raw);

        //query或者save操作
        QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));


    }
    public void xmlTest(){
        //加载xml模型文件
        DataHub.LoadXmlFile("db.xml");

        //query或者save操作
        QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));





    }
    public void dbTest(){
        //配置模型数据库
        DataHub.TemplateTableName="lader_models";
        DataHub.TemplateConn="defaultDatabase";

        //query或者save操作
        QueryAction.getCount("ticket_nodefile",new Cnd("format",".png"));





    }



}
