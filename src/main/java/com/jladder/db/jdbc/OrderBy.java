package com.jladder.db.jdbc;

import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Tuple3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/// <summary>
/// 排序处理类
/// </summary>
public class OrderBy
{
    /// <summary>
    /// 排序数据
    /// </summary>
    private Map<String,OrderStruct> _orders=new HashMap<String,OrderStruct>();
    /// <summary>
    /// 升序
    /// </summary>
    public static final int Asc = 0;
    /// <summary>
    /// 降序
    /// </summary>
    public static final  int Desc = 1;
    /// <summary>
    /// 初始化
    /// </summary>
    public OrderBy() { }

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="orderText">排序文本</param>
    public OrderBy(String orderText)
    {
        this.Push(orderText);
    }
    /// <summary>
    /// 转成文本
    /// </summary>
    /// <returns></returns>
    public String toString()
    {
        if (this._orders == null || _orders.size() < 1) return "";
        List<OrderStruct> tt = Collections.select(this._orders,(x, y)->y);
        java.util.Collections.sort(tt, Comparator.comparingInt(x -> x.index));
        final String[] orderText = {" order by "};
        tt.forEach(x->{
            orderText[0] = orderText[0] + (Strings.isBlank(x.alias) ? x.key.toLowerCase(): x.alias) + " " + x.od + ",";
        });

        return Strings.rightLess(orderText[0],1);
    }
    /// <summary>
    /// 清空排序
    /// </summary>
    public void Clear()
    {

        if (_orders==null||_orders.size()==0)return;
        if (Collections.count(_orders,(k,v) -> v.fixed) == 0)
        {
            _orders.clear();
            return;
        }
        List<String> noFixs = Collections.select(Collections.where(this._orders, (x, y) -> !y.fixed), (x, y) -> x);
        if (noFixs.size() < 1) return;
        for (String noFix : noFixs)
        {
            this._orders.remove(noFix);
        }
    }
    /// <summary>
    /// 追加排序条件(单个项目)
    /// </summary>
    /// <param name="fieldName">字段名称</param>
    /// <param name="od">排序选项(desc|asc)</param>
    /// <param name="index">序列</param>
    /// <returns>链对象</returns>
    /// <example>
    ///     orders.Push("id","asc",0);
    /// </example>
    public OrderBy Push(String fieldName, String od, int index)
    {
        if (Strings.isBlank(od)) od = "asc";
        Tuple3 old = Collections.first(_orders, (x, y) -> Core.is(x.toLowerCase() , fieldName.toLowerCase()) );

        OrderStruct orderitem=new OrderStruct();
        orderitem.key = fieldName;
        orderitem.od = od;
        orderitem.index = index;
        if (Strings.isBlank(old.item2.toString())) _orders.put(fieldName, orderitem);
        else _orders.put(old.item2.toString(),orderitem);
        return this;
    }
    public OrderBy put(String fieldname, String od){
        return put(fieldname,od,0);
    }
    /// <summary>
    /// 追加排序条件(单个项目)
    /// </summary>
    /// <param name="fieldName">字段名称</param>
    /// <param name="od">排序选项(desc|asc)</param>
    /// <param name="index">序列</param>
    /// <returns>链对象</returns>
    /// <example>
    ///     orders.Push("id","asc",0);
    /// </example>
    public OrderBy put(String fieldname, String od, int index)
    {
        if (Strings.isBlank(od)) od = "asc";
        Tuple3<Boolean,String,OrderStruct> old = Collections.first(_orders, (x, y) -> Core.is(x.toLowerCase() , fieldname.toLowerCase()) );

        OrderStruct order=new OrderStruct();
        order.key = fieldname;
        order.od = od;
        order.index = index;
        if(!old.item1){
            _orders.put(fieldname, order);
        }
        else _orders.put(old.item2,order);
        return this;
    }

    /// <summary>
    /// 追加排序条件(单个项目)
    /// </summary>
    /// <param name="fieldName">字段名称</param>
    /// <param name="parttenStr">排序选项(desc1|asc2)</param>
    /// <param name="alias">别名(desc1|asc2)</param>
    /// <returns>链对象</returns>
    public OrderBy Put(String fieldName, String parttenStr,String alias)
    {
        if (Strings.isBlank(fieldName)) return this;
        if (Strings.isBlank(parttenStr)) parttenStr = "asc";
        Matcher numberMatch = Regex.match(parttenStr, "^\\s*([a-zA-Z]*)(\\d*)$");
        OrderStruct orderItem=new OrderStruct();
        orderItem.key=fieldName;
        orderItem.alias=alias;

        if (numberMatch.find())
        {
            orderItem.index = Strings.toInt(numberMatch.group(2));
            orderItem.od = numberMatch.group(1);
        }
        Tuple3<Boolean, String, OrderStruct> first = Collections.first(_orders, (k, v) -> {
            return Core.is(k.toLowerCase(),fieldName.toLowerCase());
        });
        if(!first.item1){
            _orders.put(fieldName, orderItem);
        }else{
            _orders.put(first.item2, orderItem);
        }
        return this;
    }

    /// <summary>
    /// 追加排序条件
    /// </summary>
    /// <param name="orderText"></param>
    /// <returns>链对象</returns>
    public OrderBy Push(String orderText)
    {
        throw Core.makeThrow("未实现");
//        orderText = Regex.Replace(orderText, "^\\s*order\\s*by\\s*", "");
//        if (orderText.IsBlank()) return this;
//        List<String> gs = orderText.SplitByComma();
//        gs.ForEach(an =>
//                {
////                var newOrderField = Regex.Replace(an, "(asc|desc)\\d*\\s*$", "").Trim();
////                if (_orders.Count(d => d.Key.ToLower() == newOrderField.ToLower()) < 1)
////                {
////                }
//                        Match match = Regex.Match(an, "^\\s*([\\s\\S]*?)(asc|desc)?(\\d*)$", RegexOptions.IgnoreCase);
//        if (match.Success)
//        {
//            var key = match.Groups[1].Value.Trim();
//            var od = match.Groups[2].Value;
//            var i = match.Groups[3].Value.ToInt();
//            OrderStruct os = new OrderStruct()
//            {
//                Key = key,
//                Od = "asc",
//                Index = i
//            };
//            var ks = _orders.Keys.FirstOrDefault(x => x.ToLower() == key.ToLower());
//            if (ks.IsBlank()) _orders.Add(key, os);
//            else _orders[ks] = os;
//            if (Regex.IsMatch(od, "^[d]", RegexOptions.IgnoreCase)) os.Od = "desc";
//        }
//        else
//        {
//            var ks = _orders.Keys.FirstOrDefault(x => x.ToLower() == an.ToLower());
//            if (ks.IsBlank()) _orders.Add(ks, new OrderStruct(an));
//            else _orders[ks] = new OrderStruct(an);
//        }
//
//            });
//
//

//            orderText =Regex.Replace(orderText, "^\\s*order\\s*by\\s*", "");
//            if (Strings.isBlank(orderText)) return this;
//            string[] orderArray = orderText.Split(',');
//            foreach (var s in orderArray)
//            {
//                if (string.IsNullOrEmpty(s)) continue;
//                Match match = Regex.Match(s, "^\\s*([\\s\\S]*?)(asc|desc)?(\\d*)$", RegexOptions.IgnoreCase);
//                if (match.Success)
//                {
//                    var key = match.Groups[1].Value.Trim();
//                    var od = match.Groups[2].Value;
//                    var index = match.Groups[3].Value.ToInt();
//                    OrderStruct os = new OrderStruct()
//                    {
//                        Key = key,
//                        Od = "asc",
//                        Index = index
//                    };
//                    var ks = _orders.Keys.FirstOrDefault(x => x.ToLower() == key.ToLower());
//                    if (ks.IsBlank()) _orders.Add(key, os);
//                    else _orders[ks] = os;
//                    if (Regex.IsMatch(od, "^[d]", RegexOptions.IgnoreCase)) os.Od = "desc";
//                }
//                else
//                {
//                    var ks = _orders.Keys.Where(x => x.ToLower() == s.ToLower());
//                    if (ks == null || ks.ToList().Count == 0) _orders.Add(s, new OrderStruct(s));
//                    else _orders[ks.ToList()[0]] = new OrderStruct(s);
//                }
//            }
//        return this;
    }
    /// <summary>
    /// 更新排序字段的别名
    /// </summary>
    /// <param name="key"></param>
    /// <param name="alias"></param>
    public void UpdateAlias(String key, String alias)
    {
        if(Strings.isBlank(key))return;
        if (_orders.containsKey(key.toLowerCase()))
        {
            OrderStruct orderStruct = _orders.get(key.toLowerCase());
            if (orderStruct != null) orderStruct.alias = alias;
        }
    }
    /// <summary>
    /// 更新所有键的属性
    /// </summary>
    /// <param name="propName"></param>
    /// <param name="value"></param>
    public void UpdateAllProperty(String propName, Object value)
    {
        throw Core.makeThrow("未实现");
//        switch (propName)
//        {
//            case "fixed":
//                _orders.ForEach(x=>x.Value.Fixed=(bool)value);
//                break;
//        }
    }

    /// <summary>
    /// 删除所有排序字段的别名
    /// </summary>
    public void RemoveAllAlias()
    {
        for (OrderStruct keyValuePair : _orders.values())
        {
            if (keyValuePair != null) keyValuePair.alias = null;
        }
    }


    /// <summary>
    /// 获取原生数据
    /// </summary>
    /// <returns></returns>
    public Map<String, OrderStruct> GetRaw()
    {
        return this._orders;
    }


}