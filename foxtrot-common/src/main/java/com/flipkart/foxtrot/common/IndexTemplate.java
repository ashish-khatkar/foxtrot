package com.flipkart.foxtrot.common;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by ashish.khatkar on 18/01/18.
 */
public class IndexTemplate {

    @NotNull
    @NotEmpty
    private Map<String, Object> settings;

    @NotNull
    @NotEmpty
    private Map<String, Object> mappings;

    public IndexTemplate() {
    }

    public IndexTemplate(Map<String, Object> settings, Map<String, Object> mappings) {
        this.settings = settings;
        this.mappings = mappings;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Map<String, Object> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Object> mappings) {
        this.mappings = mappings;
    }

    @Override
    public String toString() {
        return "IndexTemplate{" +
                "settings=" + settings.toString() +
                ", mappings=" + mappings.toString() +
                '}';
    }
}
