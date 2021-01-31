package bean;

import com.jladder.db.annotation.Column;
import com.jladder.db.annotation.Table;
import com.jladder.db.bean.BaseEntity;
import com.jladder.db.enums.DbGenType;

@Table("del_sys_site")
public class MySite extends BaseEntity {

    @Column(fieldname = "id",gen = DbGenType.UUID)
    public String id;
    public String title;

    public String project;
    public String config_path;
    @Column(isExt = true)
    public String outPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getConfig_path() {
        return config_path;
    }

    public void setConfig_path(String config_path) {
        this.config_path = config_path;
    }


    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }
}
