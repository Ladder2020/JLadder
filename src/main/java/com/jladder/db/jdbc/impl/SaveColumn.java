package com.jladder.db.jdbc.impl;

import com.jladder.data.Record;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;

import java.util.*;


public class SaveColumn {
    /// <summary>
    /// 删除的列列表
    /// </summary>
    public List<String> Deletes;
    /// <summary>
    /// 匹配的列列表
    /// </summary>
    public List<String> Matchs;

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="columns"></param>
    public SaveColumn(String columns) {
        if (Strings.hasValue(columns)) {
            List<String> cs = com.jladder.lang.Collections.distinct(com.jladder.lang.Collections.toList(columns.split(",")));

            cs.forEach(x -> {
                if (x.startsWith("!") || x.startsWith("#")) {
                    if (Core.isEmpty(Deletes)) Deletes = new ArrayList<String>();
                    Deletes.add(x.replace("!", ""));
                    return;
                }
                if (Core.isEmpty(Matchs)) Matchs = new ArrayList<String>();
                Matchs.add(x);
            });

        }


    }

    /// <summary>
    /// 裁剪数据对象
    /// </summary>
    /// <param name="data"></param>
    /// <param name="columns"></param>
    /// <returns></returns>
    public static Map<String, Object> Clip(Object data, String columns) {
        if (data == null) return null;
        if (Strings.isBlank(columns)) {
            return (data instanceof Map) ? (Map<String, Object>) data : Record.parse(data);
        } else {
            SaveColumn sc = new SaveColumn(columns);
            if (!Core.isEmpty(sc.Matchs)) {

                if (data instanceof Map) {
                    Map<String, Object> raw = (Map<String, Object>) data;
                    HashMap<String, Object> ret = new HashMap<String, Object>();
                    sc.Matchs.forEach(x -> {

                        String key = com.jladder.lang.Collections.haveKey(raw, x);
                        if (Strings.isBlank(key)) return;
                        ret.put(x, raw.get(key));
                    });
                    return ret;
                } else {
                    Record raw = Record.parse(data);
                    Map<String, Object> ret = new HashMap<String, Object>();
                    sc.Matchs.forEach(x -> {
                        String key = Collections.haveKey(raw, x);
                        if (Strings.isBlank(key)) return;
                        ret.put(x, raw.get(key));
                    });
                    return ret;
                }


            }

            if (!Core.isEmpty(sc.Deletes)) {
                if (data instanceof Map) {
                    Map<String, Object> raw = (Map<String, Object>) data;
                    if (sc.Deletes != null) sc.Deletes.forEach(x -> raw.remove(x));
                    return raw;
                } else {
                    Record raw = Record.parse(data);
                    if (sc.Deletes != null) sc.Deletes.forEach(x -> raw.remove(x));
                    return raw;
                }
            }
            return data instanceof Map ? (Map<String, Object>) data : Record.parse(data);
        }
    }
}



