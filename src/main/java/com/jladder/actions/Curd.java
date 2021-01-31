package com.jladder.actions;

/// <summary>
/// 增删改查管理类
/// </summary>

import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.enums.DbSqlDataType;

public class Curd
{
    /// <summary>
    /// 表名
    /// </summary>
    public String TableName;

    /// <summary>
    /// 查询条件
    /// </summary>
    public String Condition;

    /// <summary>
    /// 分页数量
    /// </summary>
    public int Psize;

    /// <summary>
    /// 内置参数
    /// </summary>
    public String Param;

    /// <summary>
    /// 资源回馈对应
    /// </summary>
    public String Rel;

    /// <summary>
    /// 列字段
    /// </summary>
    public String Columns;

    /// <summary>
    /// 实体对象
    /// </summary>
    public String Bean;

    /// <summary>
    /// 脚本代码
    /// </summary>
    public String Script;

    /// <summary>
    /// action或执行动作
    /// </summary>
    public int Option  = 0;

    /// <summary>
    /// 执行的sql语句
    /// </summary>
    public String SqlText;

    /// <summary>
    /// 直接结果
    /// </summary>
    public Object Result;

    /// <summary>
    /// 数据库连接器
    /// </summary>
    public String Conn ;

    /// <summary>
    /// 持续数据库访问操作池
    /// </summary>
//    public KeepDaoPool KeepPool { get; set; } = null;


    public Record Parameters = new Record();


    /// <summary>
    /// 无参构造方法
    /// </summary>
    public Curd()
    {
    }

    /// <summary>
    /// 构造方法
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="bean">实体对象文本数据</param>
    /// <param name="option">选项</param>
    /// <param name="condition">条件文本</param>
    /// <param name="rel">资源回馈引用</param>
    public Curd(String tableName, String bean, int option, String condition,String rel)
    {
        this.TableName = tableName;
        this.Bean = bean;
        this.Option = option;
        this.Condition = condition;
        this.Rel = rel;
    }

    /// <summary>
    /// 构造方法
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="bean">实体对象数据</param>
    /// <param name="option">选项</param>
    /// <param name="condition">键值型条件对象</param>
    public Curd(String tableName, Object bean, DbSqlDataType option, Object condition)
    {
        TableName = tableName;

        Bean = Record.parse(bean).toString();
        Option = option.getIndex();
        if (condition instanceof Cnd)
        {
            // var cnd = condition as Cnd;
            // Condition = cnd?.GetText();
            // Parameters.Merge(cnd?.Parameters);
            Condition = ((Cnd)condition).getWhere(false,true);
        }
        else
        {
            Record record = Record.parse(condition);
            if(record!=null)Condition = record.toString(); ;
        }
    }
    /// <summary>
    /// 构造方法
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="option">选项</param>
    public Curd(String tableName, DbSqlDataType option)
    {
        this.TableName = tableName;
        this.Option = option.getIndex();
    }
    /// <summary>
    /// 设置实体对象
    /// </summary>
    /// <param name="bean"></param>
    /// <returns></returns>
    public Curd SetBean(Object bean)
    {
        this.Bean = Record.parse(bean).toString();
        return this;
    }
    /// <summary>
    /// 设置操作动作
    /// </summary>
    /// <param name="sa">动作选项</param>
    public void SetAction(DbSqlDataType sa)
    {
        this.Option = sa.getIndex();
    }
    /// <summary>
    /// 设置条件
    /// </summary>
    /// <param name="condition">条件对象</param>
    public Curd SetCondition(Object condition)
    {
        Cnd cnd = Cnd.parse(condition);
        if(cnd==null)return this;
        this.Condition = cnd.getWhere(false,true);
        return this;
    }


//        public object Execute()
//        {
//            var re_t = Execute(null);
//            this.KeepPool?.AllClose();
//            return re_t;
//        }
    /// <summary>
    ///
    /// </summary>
    /// <param name="keepDaoPool">持续数据库连接池</param>
    /// <param name="supportTran">只自创建时候有效</param>
    /// <returns></returns>

//    public Object Execute(KeepDaoPool keepDaoPool=null,bool supportTran=true)
//    {
//
//        var meCreate = false;
//        object re_t = null;
//        if (keepDaoPool == null) keepDaoPool = KeepPool;
//        if (keepDaoPool == null)
//        {
//            meCreate = true;
//            keepDaoPool =new KeepDaoPool(supportTran);
//        }
//        //含有sql语句
//        if (this.SqlText.HasValue())
//        {
//            var keepdao = keepDaoPool.CreateKeepDao(this.Conn);
//            if (SqlBasicDic.NoQueryActions.Contains(this.Option))
//            {
//                var result = keepdao.Dao.Exec(new SqlText(SqlText));
//                if (meCreate) keepDaoPool.End();
//                return result;
//            }
//            switch (this.Option)
//            {
//                case (int) SqlDataAction.Query:
//                    re_t = keepdao.Dao.Fetch(new SqlText(this.SqlText));
//                    break;
//                case (int) SqlDataAction.GetBean:
//                    re_t = keepdao.Dao.Fetch(new SqlText(this.SqlText));
//                    break;
//                case (int) SqlDataAction.GetData:
//                    re_t = keepdao.Dao.Query(new SqlText(this.SqlText));
//                    break;
//                case (int) SqlDataAction.GetValue:
//                    re_t = keepdao.Dao.GetValue<String>(new SqlText(this.SqlText));
//                    break;
//                case (int) SqlDataAction.GetCount:
//                    re_t = keepdao.Dao.GetValue<String>(new SqlText(this.SqlText));
//                    break;
//            }
//            if (meCreate) keepDaoPool.End();
//            return re_t;
//        }
//        //daohelp方式
//        else
//        {
//            var dm = DaoSeesion.GetDataModel(TableName,Param);
//            if (dm == null) return null;
//            dm.SetCondition(Condition.ToString());
//            dm.MatchColumns(Columns);
//            var keepdao = keepDaoPool.CreateKeepDao(dm.Conn);
//            switch (this.Option)
//            {
//                case (int)SqlDataAction.Query:
//                    re_t = QueryAction.GetBean(keepDaoPool, dm);
//                    break;
//                case (int)SqlDataAction.GetBean:
//                    re_t = QueryAction.GetBean(keepDaoPool, dm);
//                    break;
//                case (int)SqlDataAction.GetData:
//                    re_t = QueryAction.GetData(keepDaoPool, dm);
//                    break;
//                case (int)SqlDataAction.GetValue:
//                    re_t = keepdao.Dao.GetValue<String>(dm.SqlText());
//                    break;
//                case (int)SqlDataAction.GetCount:
//                    re_t = keepdao.Dao.Count(dm.TableName, dm.GetWhere());
//                    break;
//                default:
//                    if (SqlBasicDic.NoQueryActions.Contains(this.Option))
//                    {
//                        Record record = null;
//                        if (Option == (int) SqlDataAction.Insert || Option == (int) SqlDataAction.Update ||
//                                Option == (int) SqlDataAction.Save) record = GenBeanTool.GenBean(dm, Bean, Option);
//                        re_t = SaveAction.SaveBean(keepDaoPool,dm, record, Option,"","");
//                    }
//                    break;
//            }
//            if (meCreate) keepDaoPool.End();
//
//            return re_t;
//
//
//
//        }
//
//
//
//
//    }



    /// <summary>
    /// 执行CURD操作
    /// </summary>
    /// <returns></returns>
    //        public Object Execute(KeepDaoPool keepDaoPool)
    //        {
    //
    //
    //
    //
    //            object re_t = null;
    //            if (keepDaoPool == null) keepDaoPool = KeepPool;
    //            if (keepDaoPool == null) keepDaoPool = KeepPool = new KeepDaoPool(DaoSeesion.NewDao(this.Conn));
    //            IDao dao = keepDaoPool.GetDao(this.Conn);
    //            if (dao == null)
    //            {
    //                dao = DaoSeesion.NewDao(this.Conn);
    //                keepDaoPool.Put(new KeepDao()
    //                {
    //                    Dao = dao,
    //                    IsAllowClose = true,
    //                    IsManageTran = true,
    //                });
    //            }
    //            return Execute(ref dao, null);
    //        }

    /// <summary>
    /// 执行sql操作(为什么要有daohelp呢？提高效率，尽量减少对daohelp多次解析)
    /// </summary>
    /// <returns></returns>
//    public Object Execute(KeepDaoPool keepDaoPool, IDataModel daoHelp)
//    {
//        if (keepDaoPool == null) return null;
//        //如果存在直接结果则不进行后续操作
//        if (this.Result != null) return Result;
//        var dao = keepDaoPool.GetDao(this.Conn);
//        if (dao == null)
//        {
//            dao = DaoSeesion.NewDao(this.Conn);
//            keepDaoPool.Put(new KeepDao()
//            {
//                Dao = dao,
//                IsAllowClose = true,
//                IsManageTran = true,
//            });
//        }
//        return Execute(ref dao, daoHelp);
//    }


//
//    private object Execute(ref IDao dao, IDataModel daoHelp)
//    {
//        object re_t = null;
//        if (this.SqlText.HasValue())
//        {
//            if (SqlBasicDic.NoQueryActions.Contains(this.Option))
//            {
//                var result = dao.Exec(new SqlText(SqlText));
//                dao.Close();
//                return result;
//            }
//            //复制的
//            switch (this.Option)
//            {
//                case (int)SqlDataAction.Query:
//                    re_t = dao.Fetch(new SqlText(SqlText));
//                    break;
//                case (int)SqlDataAction.GetBean:
//                    re_t = dao.Fetch(new SqlText(SqlText));
//                    break;
//                case (int)SqlDataAction.GetData:
//                    re_t = dao.Query(new SqlText(SqlText));
//                    break;
//                case (int)SqlDataAction.GetValue:
//                    re_t = dao.GetValue<String>(new SqlText(SqlText));
//                    break;
//            }
//            if (re_t != null)
//            {
//                dao.Close();
//                return re_t;
//            }
//            return dao.Query(new SqlText(SqlText));
//        }
//        if (this.TableName.IsBlank()) return null;
//
//        if (daoHelp == null)
//        {
//            daoHelp = new DataModelForMap(dao, this.TableName, this.Param);
//        }
//        else
//        {
//            daoHelp.Reset(this.Param);
//        }
//
//        if (daoHelp.IsNull()) return null;
//        daoHelp.ClearCondition();
//        daoHelp.SetCondition(Condition.ToString());
//
//        switch (this.Option)
//        {
//            case (int)SqlDataAction.Query:
//                re_t = dao.Fetch(daoHelp.SqlText());
//                break;
//            case (int)SqlDataAction.GetBean:
//                re_t = dao.Fetch(daoHelp.SqlText());
//                break;
//            case (int)SqlDataAction.GetData:
//                re_t = dao.Query(daoHelp.SqlText());
//                break;
//            case (int)SqlDataAction.GetValue:
//                re_t = dao.GetValue<String>(daoHelp.SqlText());
//                break;
//            case (int)SqlDataAction.GetCount:
//                re_t = dao.Count(daoHelp.TableName, daoHelp.GetWhere());
//                break;
//        }
//        if (re_t != null) return re_t;
//        if (SqlBasicDic.NoQueryActions.Contains(this.Option))
//            return SaveAction.SaveBean(daoHelp, Bean, Option);
//        return null;
//    }
}
