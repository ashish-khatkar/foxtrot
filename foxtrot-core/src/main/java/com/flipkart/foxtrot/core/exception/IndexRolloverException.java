package com.flipkart.foxtrot.core.exception;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by ashish.khatkar on 19/12/17.
 */
public class IndexRolloverException extends FoxtrotException {

    private List<String> aliases;
    private String reason;

    protected IndexRolloverException(List<String> aliases, String reason) {
        super(ErrorCode.INDEX_ROLLOVER_EXCEPTION);
        this.aliases = aliases;
        this.reason = reason;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
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
        map.put("aliases", aliases);
        map.put("reason", reason);
        return map;
    }
}
