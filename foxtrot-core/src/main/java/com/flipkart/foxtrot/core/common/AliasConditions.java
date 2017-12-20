package com.flipkart.foxtrot.core.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;

/**
 * Created by ashish.khatkar on 18/12/17.
 */
public class AliasConditions {
    /**
     * By default max_docs value is set at 100M
     * max_docs condition will always be used by default
     */
    @Min(1)
    @JsonProperty("max_docs")
    private Long maxDocs = 100000000L;

    @Min(1)
    @JsonProperty("max_age")
    private Integer maxAgeInDays;

    public AliasConditions() {
    }

    public Long getMaxDocs() {
        return maxDocs;
    }

    public void setMaxDocs(Long maxDocs) {
        this.maxDocs = maxDocs;
    }

    public Integer getMaxAgeInDays() {
        return maxAgeInDays;
    }

    public void setMaxAgeInDays(Integer maxAgeInDays) {
        this.maxAgeInDays = maxAgeInDays;
    }
}
