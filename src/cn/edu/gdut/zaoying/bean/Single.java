package cn.edu.gdut.zaoying.bean;

import java.util.Map;

/**
 * Created by 祖荣 on 2016/1/31 0031.
 */
public class Single {
    String[] type;
    String descriptionCN;
    Map<String,Single> properties;
    String defaultValue;

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String getDescriptionCN() {
        return descriptionCN;
    }

    public void setDescriptionCN(String descriptionCN) {
        this.descriptionCN = descriptionCN;
    }

    public Map<String, Single> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Single> properties) {
        this.properties = properties;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
