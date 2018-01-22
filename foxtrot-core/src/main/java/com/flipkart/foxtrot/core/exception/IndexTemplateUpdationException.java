package com.flipkart.foxtrot.core.exception;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by ashish.khatkar on 22/01/18.
 */
public class IndexTemplateUpdationException extends FoxtrotException {
    private String tableName;
    private String reason;

    protected IndexTemplateUpdationException(String tableName, String reason) {
        super(ErrorCode.INDEX_TEMPLATE_UPDATION_EXCEPTION);
        this.tableName = tableName;
        this.reason = reason;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("table", tableName);
        map.put("reason", reason);
        return map;
    }

    @Override
    public String toString() {
        return String.format("table : %s\nreason : %s\n", tableName, reason);
    }
}
