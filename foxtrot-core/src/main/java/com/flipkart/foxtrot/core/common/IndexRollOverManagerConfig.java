package com.flipkart.foxtrot.core.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverManagerConfig {
    @Min(3600)
    @JsonProperty("interval")
    private int intervalInSeconds;

    @Min(1)
    @JsonProperty("initialDelay")
    private int initialDelayInSeconds;

    @NotNull
    private boolean active;

    @JsonProperty("conditions")
    private AliasConditions aliasConditions;

    public IndexRollOverManagerConfig() {

    }

    public int getIntervalInSeconds() {
        return intervalInSeconds;
    }

    public void setIntervalInSeconds(int intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
    }

    public int getInitialDelayInSeconds() {
        return initialDelayInSeconds;
    }

    public void setInitialDelayInSeconds(int initialDelayInSeconds) {
        this.initialDelayInSeconds = initialDelayInSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AliasConditions getAliasConditions() {
        return aliasConditions;
    }

    public void setAliasConditions(AliasConditions aliasConditions) {
        this.aliasConditions = aliasConditions;
    }
}
