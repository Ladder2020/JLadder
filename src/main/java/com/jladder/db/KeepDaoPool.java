package com.jladder.db;


import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KeepDaoPool
{
    public String Guid= Core.genUuid();
    /// <summary>
    /// 序列号
    /// </summary>
    public int Sequence= 0;
    /// <summary>
    /// 持续数据库连接对象列表
    /// </summary>
    public List<KeepDao> DaoList=new ArrayList<KeepDao>();//为什么不是Dic呢,便于再度升级，特别是daos是必须有重复的

    /// <summary>
    /// 是否支持事务
    /// </summary>
    public boolean IsSupportTran = true;

    /// <summary>
    /// 基本构造
    /// </summary>
    public KeepDaoPool() { }

    public KeepDaoPool(boolean isSupportTran)
    {
        this.IsSupportTran = isSupportTran;
    }

    /// <summary>
    /// 从Dao对象构造，注意：此dao不受锁存器管理
    /// </summary>
    /// <param name="dao"></param>
    public KeepDaoPool(IDao dao)
    {
        if (dao == null) return;
        KeepDao keepDao=new KeepDao();
        if (dao.isTransacting()) keepDao.isManageTran = false;
        keepDao.isAllowClose = false;
        IncreaseSequence();
        keepDao.sequence = Sequence;
        keepDao.Dao = dao;
        DaoList.add(keepDao);
        //当外部Dao不容许管理事务时，池中的事务由外部Dao事务引发
        if (!keepDao.isManageTran)
        {
            dao.addRollbackEvent(() ->{
                keepDao.isManageTran = true;
                AllRollBack();
            });
            dao.addCommitEvent(() ->{
                            keepDao.isManageTran = true;
                AllCommit();
            });
        }
    }
    public void IncreaseSequence()
    {
        this.Sequence++;
    }
    /// <summary>
    /// 关闭所有允许关闭的连接对象
    /// </summary>
    public void AllClose(){
        DaoList.forEach(x-> {
            if (x != null && x.isAllowClose)
            {
                x.Dao.close();
                //Daos.Remove(x);//是否真的可以
            }
        });
    }
    /// <summary>
    /// 关闭数据库连接对象
    /// </summary>
    /// <param name="markcode">标识码</param>
    public void close(String markcode)
    {
        DaoList.forEach(x->{
            if(x.isAllowClose && Core.is( x.Dao.getMarkCode(),markcode)){
                x.Dao.close();
            }
        });
    }
    /// <summary>
    /// 关闭数据库连接对象
    /// </summary>
    /// <param name="index">序号</param>
    public void close(int index)
    {
        if (index < 0 || index >= DaoList.size() || DaoList.size() == 0) return;
        KeepDao keepdao = DaoList.get(index);
        if(keepdao.isAllowClose)keepdao.Dao.close();
    }
    /// <summary>
    /// 获取数据库访问操作对象
    /// </summary>
    /// <param name="markcode">标识码</param>
    /// <returns></returns>
    public IDao getDao(String markcode)
    {
        if (Strings.isBlank(markcode)) markcode = "defaultDatabase";

        String finalMarkcode = markcode;
        Tuple2<Boolean, KeepDao> find = Collections.first(DaoList, (x -> finalMarkcode.equals(x.Dao.getMarkCode())));

        return find.item1? find.item2.Dao : null;
    }

    /**
     * 创建持续Dao对象
     * @param conn
     * @return 连接器名称
     */
    public KeepDao createKeepDao(String conn)
    {
        KeepDao keepDao = get(conn);
        if (keepDao != null) return keepDao;
        IDao dao = DaoSeesion.NewDao(conn);
        if(IsSupportTran) dao.beginTran();
        keepDao = new KeepDao();
        keepDao.isAllowClose = true;
        keepDao.isManageTran = true;
        keepDao.Dao = dao;
        put(keepDao);
        return keepDao;
    }
    /// <summary>
    /// 获取数据库访问操作对象
    /// </summary>
    /// <param name="index">序号</param>
    /// <returns></returns>
    public IDao getDao(int index)
    {
        if (index < 0 || index >= DaoList.size() || DaoList.size() == 0) return null;
        return DaoList.get(index).Dao;
    }
    /// <summary>
    /// 获取持续数据库访问操作对象
    /// </summary>
    /// <param name="markcode">标识码</param>
    /// <returns></returns>
    public KeepDao get(String conn)
    {
        if (Strings.isBlank(conn)) conn = "defaultDatabase";
        String finalMarkcode = conn;
        Tuple2<Boolean, KeepDao> find = Collections.first(DaoList, x -> finalMarkcode.equals(x.Dao.getMarkCode()));
        return find.item1 ? find.item2 :null;
    }
    /// <summary>
    /// 获取持续数据库访问操作对象
    /// </summary>
    /// <param name="index"></param>
    /// <returns></returns>
    public KeepDao get(int index)
    {
        return DaoList.get(index);
    }
    /// <summary>
    /// 放置持续对象(检查重复)
    /// </summary>
    /// <param name="keepdao">持续对象</param>
    /// <returns></returns>
    public KeepDaoPool put(KeepDao keepdao)
    {
        if (keepdao.Dao == null) return this;
        if (this.getDao(keepdao.Dao.getMarkCode()) != null) return this;
        IncreaseSequence();
        keepdao.sequence = Sequence;
        DaoList.add(keepdao);
        return this;
    }
    /// <summary>
    /// 放置持续对象(检查重复)
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <returns></returns>
    public KeepDaoPool Put1(IDao dao)
    {
        if(dao==null)return null;

        KeepDao keepDao = new KeepDao();
        keepDao.isAllowClose = true;
        keepDao.isManageTran = true;
        keepDao.Dao = dao;
        keepDao.sequence = this.Sequence;
        return Push(keepDao);
    }
    /// <summary>
    /// 追加持续数据库操作对象
    /// </summary>
    /// <param name="keepdao">持续数据库操作对象</param>
    /// <returns></returns>
    public KeepDaoPool Push(KeepDao keepdao)
    {
        if (keepdao == null) return this;
        keepdao.sequence = this.Sequence;
        this.DaoList.add(keepdao);
        return this;
    }
    /// <summary>
    /// 追加数据库操作对象(二次再组装成keepDao追加)
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <returns></returns>
    public KeepDaoPool push(IDao dao)
    {
        if (dao == null) return null;
        KeepDao keepDao = new KeepDao();
        keepDao.isAllowClose = true;
        keepDao.isManageTran = true;
        keepDao.Dao = dao;
        keepDao.sequence = Sequence;
        return Push(keepDao);
    }
    public void AllRollBack(){
        AllRollBack(true);
    }
    /// <summary>
    /// 强制回滚所有数据库事务(由锁存器创建的)
    /// </summary>
    ///
    public void AllRollBack(boolean isClose)
    {
        DaoList.forEach(x -> {
            if (x != null && x.isAllowClose&& x.isManageTran) x.Dao.rollback();
        });
        if (isClose)AllClose();
    }
    public void AllCommit(){
        AllCommit(true);
    }
    /// <summary>
    /// 强制提交所有数据库事务(由锁存器创建的)
    /// </summary>
    public void AllCommit(boolean isClose)
    {
//            if (HttpContext.Current != null)
//            {
//                var iskeep = false;
//                if (HttpContext.Current.Session["_keepdao_"]!=null&&(bool) HttpContext.Current.Session["_keepdao_"])
//                {
//                    iskeep = true;
//                    var daolist = (List<KeepDaoPool>)HttpContext.Current.Session["_daolist_"];
//                    if (daolist == null)
//                    {
//                        daolist = new List<KeepDaoPool>();
//                        HttpContext.Current.Session["_daolist_"] = daolist;
//                    }
//                    daolist.Add(this);
//                }
//                if (HttpContext.Current.Items["_keepdao_"]!=null&&(bool)HttpContext.Current.Items["_keepdao_"])
//                {
//                    iskeep = true;
//                    var daolist = (List<KeepDaoPool>)HttpContext.Current.Items["_daolist_"];
//                    if (daolist == null)
//                    {
//                        daolist = new List<KeepDaoPool>();
//                        HttpContext.Current.Items["_daolist_"] = daolist;
//                    }
//                    daolist.Add(this);
//                }
//                if(iskeep) return;
//            }

        DaoList.forEach(x -> { if (x != null && x.isAllowClose&& x.isManageTran) x.Dao.commitTran(); });
        if (isClose) AllClose();
    }
    /// <summary>
    /// 是否为空
    /// </summary>
    /// <returns></returns>
    public boolean isNull()
    {
        return DaoList.size() > 0 ? true : false;
    }
    /// <summary>
    /// 尝试提交事务
    /// </summary>
    /// <returns></returns>
    public boolean tryAllCommit()
    {

        if (Collections.first(DaoList,x -> x.finishTimes != x.takeTimes|| !x.isManageTran).item2 != null) return false;
        else
        {
            AllCommit(false);
            return true;
        }
    }
    /// <summary>
    /// 获取事务的差异数量
    /// </summary>
    /// <returns></returns>
    public int getTranDiff()
    {
        AtomicInteger tabkes = new AtomicInteger();
        AtomicInteger finishs = new AtomicInteger();
        DaoList.forEach(x ->{
            tabkes.addAndGet(x.takeTimes);
            finishs.addAndGet(x.finishTimes);
        });
        return tabkes.get() - finishs.get();
    }
    /// <summary>
    /// 设置当前的活动对象
    /// </summary>
    /// <param name="keepdao">持续数据库访问操作对象</param>
    public void setActive(KeepDao keepdao)
    {
        if (keepdao == null) return;
        DaoList.forEach(x->x.isActive=false);
        keepdao.isActive = true;
    }
    /// <summary>
    /// 获取当前活动的持续对象
    /// </summary>
    /// <returns></returns>
    public KeepDao getActive()
    {
        KeepDao dao = Collections.first(DaoList,x -> x.isActive).item2;
        return dao==null ? DaoList.get(DaoList.size()-1):dao;
    }
    /// <summary>
    /// 按序列批次号关闭数据库连接
    /// </summary>
    /// <param name="sequence">序列批次号</param>
    public void AllCloseBySequence(int sequence)
    {
        DaoList.forEach(x -> {
            if (x != null && x.isAllowClose&&x.sequence==sequence)
            {
                x.Dao.close();
                //Daos.Remove(x);//是否真的可以
            }
        });
    }
    /// <summary>
    /// 按序号尝试提交事务
    /// </summary>
    /// <param name="sequence"></param>
    /// <returns></returns>
    public boolean tryAllCommitBySequence(int sequence)
    {

        if (Collections.first(DaoList,x -> Core.is( x.sequence,sequence) && x.finishTimes != x.takeTimes).item2 != null) return false;
        else
        {
            AllCommitBySequence(sequence);
            AllCloseBySequence(sequence);
            return true;
        }
    }
    /// <summary>
    ///  按序列强制提交所有数据库事务(由锁存器创建的)
    /// </summary>
    /// <param name="sequence">序列号</param>
    public void AllCommitBySequence(int sequence)
    {
        DaoList.forEach(x -> { if (x != null && x.isAllowClose&& Core.is( x.sequence,sequence) && x.isManageTran) x.Dao.commitTran(); });
    }
    /// <summary>
    /// 按序列强制回滚所有数据库事务(由锁存器创建的)
    /// </summary>
    /// <param name="sequence">序列号</param>
    /// <param name="isClose">是否后续关闭</param>
    public void allRollBackBySequence(int sequence,boolean isClose)
    {
        DaoList.forEach(x -> { if (x != null && Core.is(x.sequence,sequence)  && x.isAllowClose && x.isManageTran) x.Dao.rollback(); });
        if (isClose) AllCloseBySequence(sequence);
    }

    /// <summary>
    /// 结束
    /// </summary>
    public void end()
    {
        if(getTranDiff()==0)AllCommit();
        else AllRollBack();
    }
    /// <summary>
    /// 保持当前连接池
    /// </summary>
    /// <param name="type">保持类型,1:本次用户，2:session</param>
    public static void holdKeep(int type)
    {
//            if(HttpContext.Current==null)return;
//            switch (type)
//            {
//                case 1:
//                    HttpContext.Current.Items["_keepdao_"] = true;
//                    break;
//                case 2:
//                    HttpContext.Current.Session["_keepdao_"] = true;
//                    break;
//                case 3:
//                    HttpContext.Current.Items["_keepdao_"] = true;
//                    HttpContext.Current.Session["_keepdao_"] = true;
//                    break;
//                default:
//                    break;
//            }
    }
    public static void freeKeep(){
       freeKeep(1);
    }
    /// <summary>
    /// 是否保持
    /// </summary>
    /// <param name="type">保持类型,1:本次用户，2:session</param>
    public static void freeKeep(int type)
    {
//            if (HttpContext.Current == null) return;
//            switch (type)
//            {
//                case 1:
//                    HttpContext.Current.Items["_keepdao_"] = false;
//                    break;
//                case 2:
//                    HttpContext.Current.Session["_keepdao_"] = false;
//                    break;
//                case 3:
//                    HttpContext.Current.Items["_keepdao_"] = false;
//                    HttpContext.Current.Session["_keepdao_"] = false;
//                    break;
//                default:
//                    break;
//            }
    }
    public static List<KeepDaoPool> GetKeep()
    {
        return GetKeep(1);
    }
    /// <summary>
    /// 获取保持的数据库连接池
    /// </summary>
    /// <param name="type">保持类型,1:本次用户，2:session</param>
    /// <returns></returns>
    public static List<KeepDaoPool> GetKeep(int type)
    {

//            if (HttpContext.Current == null) return null;
//            switch (type)
//            {
//                case 1:
//                    return (List<KeepDaoPool>)HttpContext.Current.Items["_daolist_"];
//                case 2:
//                    return (List<KeepDaoPool>)HttpContext.Current.Session["_daolist_"];
//                case 3:
//                    Record record=new Record();
//
//
//
//                    HttpContext.Current.Items["_daolist_"] = true;
//                    HttpContext.Current.Session["_daolist_"] = true;
//                    break;
//                default:
//                    break;
//            }
        return null;
    }
    /// <summary>
    /// 提交保持
    /// </summary>
    /// <param name="type">保持类型,1:本次用户，2:session</param>
    public static void CommitKeep(int type)
    {
        List<KeepDaoPool> ls = GetKeep(type);
        if(ls==null||ls.size()<1)return;
        freeKeep(type);
        ls.forEach(x -> x.AllCommit());
    }
    /// <summary>
    /// 当次请求
    /// </summary>
    public static final int Request = 1;
    /// <summary>
    /// 会话
    /// </summary>
    public static final int Session = 2;
    /// <summary>
    /// 全部
    /// </summary>
    public static final int All = 3;

}