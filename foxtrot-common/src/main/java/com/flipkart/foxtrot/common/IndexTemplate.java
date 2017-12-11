package com.flipkart.foxtrot.common;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by ashish.khatkar on 07/12/17.
 */
public class IndexTemplate {

    @NotNull
    @NotEmpty
    private String configName;

    @NotNull
    @NotEmpty
    private Map<String, Object> setting;

    @NotNull
    @NotEmpty
    private Map<String, Object> mapping;

    private IndexDetails foxtrotIndexDetails;

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Map<String, Object> getSetting() {
        return setting;
    }

    public void setSetting(Map<String, Object> setting) {
        this.setting = setting;
    }

    public Map<String, Object> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, Object> mapping) {
        this.mapping = mapping;
    }

    public IndexDetails getFoxtrotIndexDetails() {
        return foxtrotIndexDetails;
    }

    public void setFoxtrotIndexDetails(IndexDetails foxtrotIndexDetails) {
        this.foxtrotIndexDetails = foxtrotIndexDetails;
    }
}
