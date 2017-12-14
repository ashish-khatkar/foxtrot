package com.flipkart.foxtrot.core.common;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public enum RollOverConditions {
    MAX_AGE("max_age"),
    MAX_DOCS("max_docs");

    private String condition;

    RollOverConditions(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return condition;
    }
}
