package com.jladder.datalink;
import com.jladder.data.MappingInfo;
import com.jladder.data.Record;
import com.jladder.entity.DbDataLink;
import com.jladder.lang.Json;
import com.jladder.lang.TypeReference;
import java.util.List;
public class DataLinkConfig {
    private DbDataLink raw;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MappingInfo> getMappings() {
        return mappings;
    }

    public void setMappings(List<MappingInfo> mappings) {
        this.mappings = mappings;
    }

    private String type;

    private Object datasource;


    public List<MappingInfo> mappings;

    public DataLinkConfig(DbDataLink link)
    {
        if(raw==null)return;
        raw = link;
        Record source = Record.parse(raw.getDatasource());
        type = source.getString("type");
        switch (type)
        {
            case "Web":
                datasource = source.toClass(WebDataSource.class);
                break;
            case "WebService":
                datasource = source.toClass(WebServiceDataSource.class);
                break;
            case "Excel":
                datasource = source.toClass(FileDataSource.class);
                break;
            case "CSV":
                datasource = source.toClass(FileDataSource.class);
                break;
        }
    }

    public DataLinkConfig(Record data)
    {
        if (data == null) return;
        raw = data.toClass(DbDataLink.class);
        Record source = Record.parse(raw.getDatasource());
        type = source.getString("type");
        switch (type)
        {
            case "Web":
                datasource = source.toClass(WebDataSource.class);
                break;
            case "WebService":
                datasource = source.toClass(WebServiceDataSource.class);
                break;
            case "Excel":
                datasource = source.toClass(FileDataSource.class);
                break;
        }

        mappings = Json.toObject(raw.getMappings(), new TypeReference<List<MappingInfo>>() {});


    }


    public DbDataLink getRaw() {
        return raw;
    }

    public void setRaw(DbDataLink raw) {
        this.raw = raw;
    }

    public Object getDatasource() {
        return datasource;
    }

    public void setDatasource(Object datasource) {
        this.datasource = datasource;
    }
}

