package com.flipkart.foxtrot.core.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverManagerConfig {
    @Min(300)
    @JsonProperty("interval")
    private int intervalInSeconds = 300;

    @Min(1)
    @JsonProperty("initialDelay")
    private int initialDelayInSeconds = 1;

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
