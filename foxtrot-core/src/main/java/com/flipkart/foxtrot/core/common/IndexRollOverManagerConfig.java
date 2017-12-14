package com.flipkart.foxtrot.core.common;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverManagerConfig {
    @Min(3600)
    private int interval;

    @Min(1)
    private int initialDelay;

    @NotNull
    private boolean active;

    private Map<String, Object> conditions;

    public IndexRollOverManagerConfig() {

    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }
}
