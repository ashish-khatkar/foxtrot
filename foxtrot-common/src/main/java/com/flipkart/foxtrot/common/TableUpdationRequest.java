package com.flipkart.foxtrot.common;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by ashish.khatkar on 22/01/18.
 */
public class TableUpdationRequest {
    @NotNull
    @NotEmpty
    String tableName;

    @Min(1)
    @Max(180)
    Integer ttl;

    IndexTemplate indexTemplate;

    public TableUpdationRequest(String tableName) {
        this.tableName = tableName;
    }

    public TableUpdationRequest(String tableName, Integer ttl) {
        this.tableName = tableName;
        this.ttl = ttl;
    }

    public TableUpdationRequest(String tableName, IndexTemplate indexTemplate) {
        this.tableName = tableName;
        this.indexTemplate = indexTemplate;
    }

    public TableUpdationRequest(String tableName, Integer ttl, IndexTemplate indexTemplate) {
        this.tableName = tableName;
        this.ttl = ttl;
        this.indexTemplate = indexTemplate;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public IndexTemplate getIndexTemplate() {
        return indexTemplate;
    }

    public void setIndexTemplate(IndexTemplate indexTemplate) {
        this.indexTemplate = indexTemplate;
    }
}
