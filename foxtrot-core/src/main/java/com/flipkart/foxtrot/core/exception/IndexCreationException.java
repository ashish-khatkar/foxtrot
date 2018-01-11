package com.flipkart.foxtrot.core.exception;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by ashish.khatkar on 19/12/17.
 */
public class IndexCreationException extends FoxtrotException {

    private String indexName;
    private String reason;

    protected IndexCreationException(String indexName, String reason) {
        super(ErrorCode.INDEX_CREATION_EXCEPTION);
        this.indexName = indexName;
        this.reason = reason;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
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
        map.put("indexName", indexName);
        map.put("reason", reason);
        return map;
    }

    @Override
    public String toString() {
        return String.format("indexName : %s\nreason : %s\n", indexName, reason);
    }
}
