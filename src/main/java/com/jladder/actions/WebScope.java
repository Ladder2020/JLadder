package com.jladder.actions;

import com.jladder.datamodel.IDataModel;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Tuple2;

public class WebScope {



    public static Tuple2<Boolean,String> MappingConn(String conn, String tableName){
        return new Tuple2(false);
    }
    public static IDataModel MappingConn(IDataModel dm, String name)
    {
        if (dm == null) return null;
        if (Strings.isBlank(name)) name = dm.Raw.Name;
        Tuple2<Boolean, String> ret = MappingConn(dm.Conn, name);
        if (ret.item1) dm.Conn = ret.item2;
        return dm;
    }


    public static void SetDataModelConn(String tableName, String conn) {
        Core.makeThrow("未实现[026]","WebScope");
    }

    public static void setConn(String conn) {
        Core.makeThrow("未实现[030]","WebScope");
    }
}
