package com.jladder.db.jdbc;

import com.jladder.db.Cnd;
import com.jladder.lang.Collections;
import com.jladder.lang.Strings;

import java.util.ArrayList;
import java.util.List;

public class GroupBy
{
    /// <summary>
    /// 片选字段
    /// </summary>
    private List<String> _cs = new ArrayList<String>();
    /// <summary>
    /// 分组条件
    /// </summary>
    private String _having ="";
    /// <summary>
    /// 初始化
    /// </summary>
    public GroupBy() { }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="groupText">分组文本</param>
    public GroupBy(String groupText)
    {
        if (Strings.isBlank(groupText)) return;
        addGroup(groupText);
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="cnd">条件对象</param>
    public GroupBy(Cnd cnd)
    {
        _having = cnd.getWhere(false,true);
    }
    /// <summary>
    /// 文本化
    /// </summary>
    /// <returns></returns>
    public String toString()
    {
        final String[] groupText = {""};
        if (this._cs.size() == 0) return groupText[0];
        _cs.forEach(x -> groupText[0] += x+",");
        groupText[0] = Strings.rightLess(groupText[0], 1);
        if (!Strings.isBlank(_having)) groupText[0] += " having " + _having;
        return " group by "+ groupText[0];
    }
    /// <summary>
    /// 修改条件文本
    /// </summary>
    /// <param name="havingText">条件文本</param>
    public void having(String havingText)
    {
        if (Strings.isBlank(havingText)) this._having = "";
        this._having = havingText;
    }
    /// <summary>
    /// 增加分组条件
    /// </summary>
    /// <param name="cnd">条件对象</param>
    /// <returns></returns>
    public GroupBy pushHaving(Cnd cnd)
    {
        this._having += " and " + cnd.getWhere(false,true);
        return this;
    }
    /// <summary>
    /// 增加分组条件
    /// </summary>
    /// <param name="cndStr">条件文本</param>
    /// <returns></returns>
    public GroupBy pushHaving(String cndStr)
    {
        this._having += " and " + cndStr;
        return this;
    }
    /// <summary>
    /// 增加分组字段
    /// </summary>
    /// <param name="cstr">字段文本</param>
    /// <returns></returns>
    public GroupBy pushGroup(String groupText)
    {
        if (Strings.isBlank(groupText)) return this;
        addGroup(groupText);
        return this;
    }
    /// <summary>
    /// 添加一个分组项[内部方法]
    /// </summary>
    /// <param name="text">分组项文本</param>
    public void addGroup(String text)
    {
        if (Strings.isBlank(text)) return;
        List<String> gs = Strings.splitByComma(text);
        gs.forEach(x -> {
            if (!Strings.isBlank(x) && !Collections.any(_cs, d -> d.equals(x.toLowerCase()))) this._cs.add(x);
        });
    }

    /// <summary>
    /// 清空分组
    /// </summary>
    public void clear()
    {
        this._cs.clear();
        this._having = "";
    }

}